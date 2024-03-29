/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.structEditor;

import static com.celements.common.MoreObjectsCel.*;
import static com.celements.common.MoreOptional.*;
import static com.celements.common.MoreOptional.findFirstPresent;
import static com.celements.common.lambda.LambdaExceptionUtil.*;
import static com.celements.structEditor.classes.StructuredDataEditorClass.*;
import static java.util.function.Predicate.*;
import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.cells.CellRenderStrategy;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.ClassIdentity;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.CustomClassField;
import com.celements.model.context.ModelContext;
import com.celements.model.field.FieldAccessor;
import com.celements.model.field.StringFieldAccessor;
import com.celements.model.field.XObjectFieldAccessor;
import com.celements.model.field.XObjectStringFieldAccessor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.structEditor.classes.FormFieldEditorClass;
import com.celements.structEditor.classes.OptionTagEditorClass;
import com.celements.structEditor.classes.StructuredDataEditorClass;
import com.celements.tag.CelTag;
import com.celements.tag.CelTagService;
import com.celements.tag.classdefs.CelTagClass;
import com.celements.velocity.VelocityService;
import com.celements.web.classes.KeyValueClass;
import com.celements.web.comparators.BaseObjectComparator;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.BooleanClass;
import com.xpn.xwiki.objects.classes.DateClass;
import com.xpn.xwiki.objects.classes.ListClass;
import com.xpn.xwiki.objects.classes.ListItem;
import com.xpn.xwiki.objects.classes.PropertyClass;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

@Component
public class DefaultStructuredDataEditorService implements StructuredDataEditorService {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(DefaultStructuredDataEditorService.class);

  @Requirement
  private Execution exec;

  @Requirement
  private IPageTypeResolverRole ptResolver;

  @Requirement
  private IWebUtilsService webUtils;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelContext context;

  @Requirement
  protected VelocityService velocityService;

  @Requirement(XObjectFieldAccessor.NAME)
  protected FieldAccessor<BaseObject> xObjFieldAccessor;

  @Requirement(XObjectStringFieldAccessor.NAME)
  protected StringFieldAccessor<BaseObject> xObjStrFieldAccessor;

  @Inject
  private CelTagService celTagService;

  @Override
  public Optional<String> getAttributeName(XWikiDocument cellDoc, XWikiDocument onDoc) {
    List<String> nameParts = new ArrayList<>(3);
    getCellFieldName(cellDoc).ifPresent(fieldName -> {
      getCellClassRef(cellDoc).ifPresent(classRef -> {
        nameParts.add(classRef.serialize());
        if (onDoc != null) {
          nameParts.add(Integer.toString(tryDetermineObjNb(cellDoc, onDoc)
              .orElseGet(() -> getCreateObjNb(cellDoc))));
        }
      });
      nameParts.add(fieldName);
    });
    String name = nameParts.stream().collect(joining("_"));
    LOGGER.info("getAttributeName: '{}' for cell '{}', onDoc '{}'", name, cellDoc, onDoc);
    return asNonBlank(name);
  }

  private Optional<Integer> tryDetermineObjNb(XWikiDocument cellDoc, XWikiDocument onDoc) {
    return findFirstPresent(
        () -> getContextDependentObjNb(cellDoc)
            .filter(nb -> (nb < 0) || newLangXObjFetcher(cellDoc, onDoc).filter(nb).exists()),
        () -> newLangXObjFetcher(cellDoc, onDoc).stream().findFirst()
            .map(BaseObject::getNumber));
  }

  /**
   * @return a execution-unique and -persistent create objNb for every class/key/value combination
   */
  private int getCreateObjNb(XWikiDocument cellDoc) {
    ClassReference classRef = getCellClassRef(cellDoc).orElseThrow(IllegalStateException::new);
    Map<String, Integer> objNbs = getCreateObjNbExecutionCache()
        .computeIfAbsent(classRef, ref -> new HashMap<>());
    String keyValueId = fetchKeyValues(cellDoc, Sets.union(LABELS_AND, LABELS_OR))
        .mapKeyValue((key, val) -> key + ":" + val.orElse(""))
        .joining(",");
    return objNbs.computeIfAbsent(keyValueId, key -> -(1 + objNbs.size()));
  }

  private Map<ClassReference, Map<String, Integer>> getCreateObjNbExecutionCache() {
    return exec.getContext().computeIfAbsent("struct_create_obj_nbs", HashMap::new);
  }

  @Override
  public Optional<String> getPrettyName(XWikiDocument cellDoc) {
    String prettyName = "";
    String dictKey = Stream.of(
        resolveFormPrefix(cellDoc),
        getAttributeName(cellDoc, null),
        getOptionTagValue(cellDoc))
        .flatMap(Optional::stream)
        .collect(joining("_"));
    LOGGER.debug("getPrettyName: dictKey '{}' for cell '{}'", dictKey, cellDoc);
    prettyName = webUtils.getAdminMessageTool().get(dictKey);
    if (dictKey.equals(prettyName)) {
      prettyName = getXClassPrettyName(cellDoc).orElse(dictKey);
    }
    LOGGER.info("getPrettyName: '{}' for cell '{}'", prettyName, cellDoc);
    return asNonBlank(prettyName);
  }

  private Optional<String> getOptionTagValue(XWikiDocument cellDoc) {
    return XWikiObjectFetcher.on(cellDoc)
        .fetchField(OptionTagEditorClass.FIELD_VALUE)
        .stream().findFirst();
  }

  @Override
  public Optional<String> getDateFormatFromField(XWikiDocument cellDoc) {
    Optional<PropertyClass> field = getCellPropertyClass(cellDoc);
    if (field.isPresent() && (field.get() instanceof DateClass)) {
      DateClass dateField = (DateClass) field.get();
      return Optional.ofNullable(dateField.getDateFormat());
    }
    return Optional.empty();
  }

  Optional<String> resolveFormPrefix(XWikiDocument cellDoc) {
    Optional<String> prefix = modelAccess.streamParents(cellDoc)
        .flatMap(parentDoc -> XWikiObjectFetcher.on(parentDoc)
            .fetchField(FormFieldEditorClass.FIELD_PREFIX)
            .stream())
        .findFirst();
    LOGGER.debug("resolveFormPrefix: '{}' for cell '{}'", prefix, cellDoc);
    return prefix;
  }

  Optional<String> getXClassPrettyName(XWikiDocument cellDoc) {
    String prettyName = null;
    Optional<PropertyClass> property = getCellPropertyClass(cellDoc);
    if (property.isPresent()) {
      prettyName = Strings.emptyToNull(property.get().getPrettyName());
    }
    LOGGER.debug("getXClassPrettyName: '{}' for cell '{}'", prettyName, cellDoc);
    return Optional.ofNullable(prettyName);
  }

  @Override
  public Optional<ClassReference> getCellClassRef(XWikiDocument cellDoc) {
    return XWikiObjectFetcher.on(cellDoc)
        .fetchField(StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS)
        .stream().findFirst()
        .map(ClassReference::new);
  }

  @Override
  public Optional<String> getCellFieldName(XWikiDocument cellDoc) {
    return XWikiObjectFetcher.on(cellDoc)
        .fetchField(StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME)
        .stream().findFirst();
  }

  @Override
  public Optional<BaseClass> getCellXClass(XWikiDocument cellDoc) {
    return getCellClassRef(cellDoc)
        .map(classRef -> classRef.getDocRef(cellDoc.getDocumentReference().getWikiReference()))
        .map(modelAccess::getOrCreateDocument)
        .filter(not(XWikiDocument::isNew))
        .map(XWikiDocument::getXClass);
  }

  @Override
  public Optional<PropertyClass> getCellPropertyClass(XWikiDocument cellDoc) {
    return getCellXClass(cellDoc)
        .flatMap(xClass -> getCellFieldName(cellDoc)
            .map(xClass::get))
        .flatMap(prop -> tryCast(prop, PropertyClass.class));
  }

  Optional<ClassField<?>> getCellClassField(XWikiDocument cellDoc) {
    return getCellClassRef(cellDoc)
        .flatMap(ClassIdentity::getClassDefinition)
        .flatMap(classDef -> classDef.getField(getCellFieldName(cellDoc).orElse("")));
  }

  @Override
  public Optional<String> getCellValueAsString(XWikiDocument cellDoc, XWikiDocument onDoc) {
    return getCellValue(cellDoc, onDoc)
        .flatMap(value -> trySerializeForCustomClassField(cellDoc, value))
        .filter(not(String::isEmpty));
  }

  @SuppressWarnings("unchecked")
  Optional<String> trySerializeForCustomClassField(XWikiDocument cellDoc, Object value) {
    return tryCast(value, String.class)
        .or(() -> tryCast(getCellClassField(cellDoc).orElse(null), CustomClassField.class)
            .filter(f -> f.getType().isAssignableFrom(value.getClass()))
            .flatMap(f -> f.serialize(value)))
        .or(() -> Optional.ofNullable(value)
            .map(Objects::toString));
  }

  @Override
  public Optional<String> getRequestOrCellValue(XWikiDocument cellDoc, XWikiDocument onDoc) {
    return getAttributeName(cellDoc, onDoc)
        .flatMap(context::getRequestParam)
        .or(() -> getCellValueAsString(cellDoc, onDoc));
  }

  @Override
  public Optional<Date> getCellDateValue(XWikiDocument cellDoc, XWikiDocument onDoc) {
    return getCellValue(cellDoc, onDoc)
        .flatMap(tryCastOpt(Date.class));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<String> getCellListValue(XWikiDocument cellDoc, XWikiDocument onDoc) {
    Collection<Object> values = getCellValue(cellDoc, onDoc)
        .map(o -> tryCast(o, Collection.class).orElseGet(() -> List.of(o)))
        .orElseGet(List::of);
    return values.stream()
        .map(elem -> (elem != null) ? elem.toString() : "")
        .collect(toList());
  }

  @Override
  public Optional<Object> getCellValue(XWikiDocument cellDoc, XWikiDocument onDoc) {
    Optional<String> fieldName = getCellFieldName(cellDoc);
    Object value = null;
    if (fieldName.isPresent()) {
      Optional<BaseObject> obj = getXObjectInStructEditor(cellDoc, onDoc);
      if (obj.isPresent()) {
        value = xObjStrFieldAccessor.get(obj.get(), fieldName.get()).orElse(null);
      } else if (fieldName.get().equals("title")) {
        value = getTranslatedValue(onDoc, XWikiDocument::getTitle);
      } else if (fieldName.get().equals("content")) {
        value = getTranslatedValue(onDoc, XWikiDocument::getContent);
      }
    }
    return Optional.ofNullable(value);
  }

  private String getTranslatedValue(XWikiDocument onDoc,
      Function<XWikiDocument, String> valueGetter) {
    String value = null;
    if (onDoc != null) {
      onDoc = modelAccess.getDocumentOpt(onDoc.getDocumentReference(),
          context.getLanguage().orElseGet(context::getDefaultLanguage))
          .orElse(onDoc);
      value = Strings.emptyToNull(valueGetter.apply(onDoc).trim());
    }
    return value;
  }

  @Override
  public Optional<BaseObject> getXObjectInStructEditor(XWikiDocument cellDoc, XWikiDocument onDoc) {
    Optional<BaseObject> ret = Optional.empty();
    Optional<ClassReference> classRef = getCellClassRef(cellDoc);
    if (classRef.isPresent() && (onDoc != null)) {
      XWikiObjectFetcher fetcher = newXObjFetcher(cellDoc, onDoc);
      getContextDependentObjNb(cellDoc)
          .or(() -> getNumberForMultilingual(cellDoc, onDoc))
          .ifPresent(fetcher::filter);
      ret = fetcher.stream().findFirst();
    }
    LOGGER.info("getXObjectInStructEditor - for cellDoc '{}', onDoc '{}', class '{}', objNb '{}': "
        + "{}", cellDoc, onDoc, classRef.orElse(null), ret.map(BaseObject::getNumber).orElse(null),
        ret.orElse(null));
    return ret;
  }

  @Override
  public Stream<BaseObject> streamXObjectsForCell(XWikiDocument cellDoc, XWikiDocument onDoc) {
    LOGGER.trace("streamXObjectsForCell: for [{}] on [{}]", cellDoc, onDoc);
    Stream<BaseObject> objs = (onDoc != null)
        ? newXObjFetcher(cellDoc, onDoc).stream()
        : Stream.empty();
    Comparator<BaseObject> comp = getCellFieldName(cellDoc)
        .map(field -> Arrays.asList(field.split(",")))
        .flatMap(BaseObjectComparator::create)
        .orElse(null);
    return (comp != null) ? objs.sorted(comp) : objs;
  }

  private Optional<Integer> getContextDependentObjNb(XWikiDocument cellDoc) {
    Optional<Integer> ret = findFirstPresent(
        () -> getNumberFromRequest(cellDoc),
        () -> getNumberFromExecutionContext(),
        () -> getNumberFromComputedField(cellDoc));
    LOGGER.debug("getContextDependentObjNb: got [{}] for [{}]", ret, cellDoc);
    return ret;
  }

  private Optional<Integer> getNumberFromRequest(XWikiDocument cellDoc) {
    ClassReference classRef = getCellClassRef(cellDoc).orElseThrow(IllegalStateException::new);
    return StreamEx.of("objNb")
        .append(buildNumberRequestKey(classRef, EntryStream.empty()))
        .append(buildNumberRequestKey(classRef, fetchKeyValues(cellDoc, LABELS_AND)))
        .append(fetchKeyValues(cellDoc, LABELS_OR)
            .mapKeyValue((key, val) -> buildNumberRequestKey(classRef, EntryStream.of(key, val))))
        .map(context::getRequestParam)
        .flatMap(Optional::stream)
        .map(Ints::tryParse)
        .filter(Objects::nonNull)
        .findFirst();
  }

  public static String buildNumberRequestKey(ClassReference classRef,
      EntryStream<String, Optional<String>> filter) {
    String filterPart = filter.mapKeyValue((key, val) -> key.trim()
        + (!key.isBlank() && val.isPresent() ? ":" : "")
        + val.orElse(""))
        .filter(not(String::isBlank))
        .joining(",");
    return "objNb_" + classRef.serialize() + (!filterPart.isEmpty() ? "_" : "") + filterPart;
  }

  private Optional<Integer> getNumberFromExecutionContext() {
    return Stream.of("objNb", CellRenderStrategy.EXEC_CTX_KEY_OBJ_NB,
        CellRenderStrategy.EXEC_CTX_KEY_GLOBAL_OBJ_NB)
        .map(exec.getContext()::getProperty)
        .map(Objects::toString)
        .map(Ints::tryParse)
        .filter(Objects::nonNull)
        .findFirst();
  }

  private Optional<Integer> getNumberFromComputedField(XWikiDocument cellDoc) {
    return XWikiObjectFetcher.on(cellDoc).filter(CLASS_REF).stream()
        .map(obj -> getVelocityFieldValue(obj, FIELD_COMPUTED_OBJ_NB))
        .flatMap(Optional::stream)
        .map(Ints::tryParse)
        .filter(Objects::nonNull)
        .findFirst();
  }

  private Optional<Integer> getNumberForMultilingual(XWikiDocument cellDoc, XWikiDocument onDoc) {
    return newLangXObjFetcher(cellDoc, onDoc)
        .stream().findFirst()
        .map(BaseObject::getNumber);
  }

  XWikiObjectFetcher newLangXObjFetcher(XWikiDocument cellDoc, XWikiDocument onDoc) {
    return isMultilingual(cellDoc)
        ? newXObjFetcher(cellDoc, onDoc).filter(this::isOfRequestOrDefaultLang)
        : newXObjFetcher(cellDoc, onDoc);
  }

  private boolean isOfRequestOrDefaultLang(BaseObject xObj) {
    return getLangDependent(name -> xObjStrFieldAccessor.get(xObj, name).orElse(null)).orElse("")
        .equals(context.getLanguage().orElseGet(() -> context.getDefaultLanguage(
            xObj.getDocumentReference())));
  }

  private <T> Optional<T> getLangDependent(Function<String, T> func) {
    return ClassDefinition.LANG_FIELD_NAMES.stream()
        .map(func)
        .filter(Objects::nonNull)
        .findFirst();
  }

  @Override
  public boolean hasEditField(XWikiDocument cellDoc) {
    return getCellFieldName(cellDoc).isPresent();
  }

  @Override
  public boolean isMultilingual(XWikiDocument cellDoc) {
    return XWikiObjectFetcher.on(cellDoc)
        .fetchField(FIELD_MULTILINGUAL)
        .stream().findFirst()
        .orElse(false);
  }

  @Override
  public Optional<String> getLangNameAttribute(XWikiDocument cellDoc, XWikiDocument onDoc) {
    return Optional.of(cellDoc)
        .filter(this::isMultilingual)
        .flatMap(this::getCellXClass)
        .flatMap(xClass -> getLangDependent(xClass::get))
        .flatMap(prop -> getAttributeName(cellDoc, onDoc)
            .flatMap(attrName -> getCellFieldName(cellDoc)
                .map(fieldName -> attrName.replace(fieldName, prop.getName()))));
  }

  @Override
  public Map<String, String> getCellPossibleValues(XWikiDocument cellDoc) {
    return determineTagType(cellDoc)
        .map(this::getPossibleValuesByTagType)
        .orElseGet(() -> getPossibleValuesFromXClass(cellDoc));
  }

  private Optional<String> determineTagType(XWikiDocument cellDoc) {
    if (getCellClassField(cellDoc).filter(CelTagClass.FIELD_TAGS::equals).isEmpty()) {
      return Optional.empty(); // celldoc not configured to CelTagClass.FIELD_TAGS
    }
    // the tag type is configured in KeyValueClass
    return fetchKeyValues(cellDoc, Sets.union(LABELS_AND, LABELS_OR))
        .filterKeys(CelTagClass.FIELD_TYPE.getName()::equals)
        .values().flatMap(Optional::stream)
        .findFirst();
  }

  private Map<String, String> getPossibleValuesByTagType(String tagType) {
    String lang = webUtils.getAdminLanguage();
    return celTagService.streamTags(tagType)
        .sorted(CelTag.CMP_DEFAULT.apply(lang))
        .filter(tag -> tag.hasScope(context.getWikiRef()))
        .mapToEntry(CelTag::getName, tag -> tag.getAncestorsAndThis()
            .map(t -> t.getPrettyName(lang).orElse(null))
            .joining(" &#x27A4; "))
        .toCustomMap(LinkedHashMap::new);
  }

  private Map<String, String> getPossibleValuesFromXClass(XWikiDocument cellDoc) {
    var ret = new LinkedHashMap<String, String>();
    PropertyClass propClass = getCellPropertyClass(cellDoc).orElse(null);
    if (propClass instanceof BooleanClass) {
      ret.put("0", webUtils.getAdminMessageTool().get("cel_no"));
      ret.put("1", webUtils.getAdminMessageTool().get("cel_yes"));
    } else if (propClass instanceof ListClass) {
      ListClass listClass = (ListClass) propClass;
      Map<String, ListItem> listItems = listClass.getMap(context.getXWikiContext());
      for (var val : listClass.getList(context.getXWikiContext())) {
        ret.put(val, Optional.ofNullable(listItems.get(val)).map(ListItem::getValue).orElse(null));
      }
    }
    return ret;
  }

  XWikiObjectFetcher newXObjFetcher(XWikiDocument cellDoc, XWikiDocument onDoc) {
    XWikiObjectFetcher fetcher = XWikiObjectFetcher.on(onDoc);
    getCellClassRef(cellDoc).ifPresent(fetcher::filter);
    getKeyValueXObjFilters(cellDoc, LABELS_AND).reduce((a, b) -> a.and(b))
        .ifPresent(fetcher::filter);
    getKeyValueXObjFilters(cellDoc, LABELS_OR).reduce((a, b) -> a.or(b))
        .ifPresent(fetcher::filter);
    return fetcher;
  }

  private Stream<Predicate<BaseObject>> getKeyValueXObjFilters(XWikiDocument cellDoc,
      Collection<String> labels) {
    return fetchKeyValues(cellDoc, labels).mapKeyValue(
        (key, val) -> (obj -> Objects.equals(xObjStrFieldAccessor.get(obj, key), val)));
  }

  EntryStream<String, Optional<String>> fetchKeyValues(XWikiDocument cellDoc,
      Collection<String> labels) {
    XWikiObjectFetcher fetcher = XWikiObjectFetcher.on(cellDoc)
        .filter(KeyValueClass.FIELD_LABEL, labels);
    return StreamEx.of(fetcher.stream()).mapToEntry(
        kvObj -> getVelocityFieldValue(kvObj, KeyValueClass.FIELD_KEY),
        kvObj -> getVelocityFieldValue(kvObj, KeyValueClass.FIELD_VALUE))
        .flatMapKeys(Optional::stream);
  }

  private Optional<String> getVelocityFieldValue(BaseObject obj, ClassField<String> field) {
    Optional<String> value = xObjFieldAccessor.get(obj, field)
        .filter(not(String::isBlank));
    try {
      return value.map(rethrowFunction(text -> velocityService.evaluateVelocityText(text)));
    } catch (XWikiVelocityException exc) {
      LOGGER.warn("getFieldValue - failed for [{}], [{}]", obj, field, exc);
    }
    return value;
  }

}
