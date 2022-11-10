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
import static com.google.common.base.Predicates.*;
import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.common.MoreOptional;
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
import com.celements.velocity.VelocityService;
import com.celements.web.classes.KeyValueClass;
import com.celements.web.comparators.BaseObjectComparator;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.DateClass;
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

  @Override
  public Optional<String> getAttributeName(XWikiDocument cellDoc, XWikiDocument onDoc) {
    List<String> nameParts = new ArrayList<>(3);
    getCellFieldName(cellDoc).ifPresent(fieldName -> {
      getCellClassRef(cellDoc).ifPresent(classRef -> {
        nameParts.add(modelUtils.serializeRef(classRef));
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
    return modelUtils.computeExecPropIfAbsent("struct_create_obj_nbs", HashMap::new);
  }

  @Override
  public Optional<String> getPrettyName(XWikiDocument cellDoc) {
    String prettyName = "";
    String dictKey = Stream.of(
        resolveFormPrefix(cellDoc),
        getAttributeName(cellDoc, null),
        getOptionTagValue(cellDoc))
        .flatMap(MoreOptional::stream)
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

  private Optional<ClassField<?>> getCellClassField(XWikiDocument cellDoc) {
    return getCellClassRef(cellDoc)
        .flatMap(ClassIdentity::getClassDefinition)
        .flatMap(classDef -> classDef.getField(getCellFieldName(cellDoc).orElse("")));
  }

  @Override
  public Optional<String> getCellValueAsString(XWikiDocument cellDoc, XWikiDocument onDoc) {
    return Optional.ofNullable(getCellValue(cellDoc, onDoc))
        .flatMap(value -> trySerializeForCustomClassField(cellDoc, value))
        .map(Objects::toString)
        .filter(not(String::isEmpty));
  }

  @SuppressWarnings("unchecked")
  private Optional<?> trySerializeForCustomClassField(XWikiDocument cellDoc, Object value) {
    if (value instanceof String) {
      return Optional.of(value);
    }
    ClassField<?> field = getCellClassField(cellDoc).orElse(null);
    try {
      if (field instanceof CustomClassField) {
        return ((CustomClassField<Object>) field).serialize(value);
      } else {
        return Optional.ofNullable(value);
      }
    } catch (ClassCastException cce) {
      LOGGER.warn("trySerializeForCustomClassField: unable to cast [{}] for [{}] on [{}]",
          value, field, cellDoc.getDocumentReference());
      return Optional.empty();
    }
  }

  @Override
  public Optional<Date> getCellDateValue(XWikiDocument cellDoc, XWikiDocument onDoc) {
    Object value = getCellValue(cellDoc, onDoc);
    if (value instanceof Date) {
      return Optional.of((Date) value);
    }
    return Optional.empty();
  }

  @Override
  public List<String> getCellListValue(XWikiDocument cellDoc, XWikiDocument onDoc) {
    List<String> ret = new ArrayList<>();
    Object value = getCellValue(cellDoc, onDoc);
    if (value instanceof List) {
      for (Object elem : (List<?>) value) {
        ret.add(elem != null ? elem.toString() : "");
      }
    }
    return ret;
  }

  private Object getCellValue(XWikiDocument cellDoc, XWikiDocument onDoc) {
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
    return value;
  }

  private String getTranslatedValue(XWikiDocument onDoc,
      Function<XWikiDocument, String> valueGetter) {
    String value = null;
    if (onDoc != null) {
      try {
        onDoc = onDoc.getTranslatedDocument(context.getXWikiContext());
      } catch (XWikiException exc) {
        // is actually never thrown in #getTranslatedDocument
        LOGGER.error("getTranslatedValue - [{}]", onDoc, exc);
      }
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
      getContextDependentObjNb(cellDoc).map(Optional::of) // replace with Optional#or in Java9+
          .orElseGet(() -> getNumberForMultilingual(cellDoc, onDoc))
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
    Stream<BaseObject> objs = (onDoc != null)
        ? newXObjFetcher(cellDoc, onDoc).stream()
        : Stream.empty();
    getCellFieldName(cellDoc).ifPresent(fieldName -> objs.sorted(new BaseObjectComparator(
        fieldName.replaceFirst("-", ""), !fieldName.startsWith("-"), null, false)));
    return objs;
  }

  private Optional<Integer> getContextDependentObjNb(XWikiDocument cellDoc) {
    Optional<Integer> ret = findFirstPresent(
        () -> getNumberFromRequest(),
        () -> getNumberFromExecutionContext(),
        () -> getNumberFromComputedField(cellDoc));
    ret.ifPresent(nb -> LOGGER.debug("getContextDependentObjNb: got [{}] for [{}]", nb, cellDoc));
    return ret;
  }

  private Optional<Integer> getNumberFromRequest() {
    return Optional.ofNullable(Ints.tryParse(context.getRequestParameter("objNb").or("")));
  }

  private Optional<Integer> getNumberFromExecutionContext() {
    return Stream.of("objNb", "celements.globalvalues.cell.number")
        .map(exec.getContext()::getProperty)
        .map(Objects::toString)
        .map(Ints::tryParse)
        .filter(Objects::nonNull)
        .findFirst();
  }

  private Optional<Integer> getNumberFromComputedField(XWikiDocument cellDoc) {
    return XWikiObjectFetcher.on(cellDoc).filter(CLASS_REF).stream()
        .map(obj -> getVelocityFieldValue(obj, FIELD_COMPUTED_OBJ_NB))
        .flatMap(MoreOptional::stream)
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
        .flatMapKeys(MoreOptional::stream);
  }

  private Optional<String> getVelocityFieldValue(BaseObject obj, ClassField<String> field) {
    Optional<String> value = xObjFieldAccessor.get(obj, field)
        .flatMap(MoreOptional::asNonBlank);
    try {
      return value.map(rethrowFunction(text -> velocityService.evaluateVelocityText(text)));
    } catch (XWikiVelocityException exc) {
      LOGGER.warn("getFieldValue - failed for [{}], [{}]", obj, field, exc);
    }
    return value;
  }

}
