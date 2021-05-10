package com.celements.struct.edit.autocomplete;

import static com.celements.common.lambda.LambdaExceptionUtil.*;
import static com.celements.structEditor.classes.SelectTagAutocompleteEditorClass.*;
import static com.google.common.base.Predicates.*;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.util.ModelUtils;
import com.celements.sajson.JsonBuilder;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.web.IWebSearchService;
import com.celements.search.web.classes.WebSearchConfigClass;
import com.celements.structEditor.StructuredDataEditorService;
import com.celements.structEditor.classes.OptionTagEditorClass;
import com.celements.velocity.VelocityService;
import com.google.common.base.Strings;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class DefaultAutocomplete implements AutocompleteRole {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  @Requirement
  protected StructuredDataEditorService structEditService;

  @Requirement
  protected IWebSearchService webSearchService;

  @Requirement
  protected VelocityService velocityService;

  @Requirement
  protected IModelAccessFacade modelAccess;

  @Requirement
  protected ModelContext context;

  @Requirement
  protected ModelUtils modelUtils;

  @Override
  public String getName() {
    return "default";
  }

  @Override
  public String getJsFilePath() {
    return ""; // no additional JS required for default implementation
  }

  /**
   * default implementation may be configured like any web search on the layout cell doc,
   * see {@link WebSearchConfigClass}
   */
  @Override
  public @NotNull LuceneSearchResult search(DocumentReference cellDocRef, String searchTerm) {
    return webSearchService.webSearch(searchTerm, (cellDocRef == null) ? null
        : modelAccess.getOrCreateDocument(cellDocRef));
  }

  @Override
  public JsonBuilder getJsonForValue(DocumentReference onDocRef, DocumentReference cellDocRef) {
    JsonBuilder jsonBuilder = new JsonBuilder();
    jsonBuilder.openDictionary();
    if (onDocRef != null) {
      jsonBuilder.addProperty("fullName", modelUtils.serializeRef(onDocRef));
      jsonBuilder.addProperty("name", displayNameForValue(onDocRef, cellDocRef));
      renderResultFromCell(cellDocRef, FIELD_RESULT_HTML, onDocRef)
          .ifPresent(html -> jsonBuilder.addProperty("html", html));
    }
    jsonBuilder.closeDictionary();
    return jsonBuilder;
  }

  @Override
  public String displayNameForValue(DocumentReference onDocRef, DocumentReference cellDocRef) {
    return findFirstPresent(Stream.concat(
        Stream.of(() -> renderResultFromCell(cellDocRef, FIELD_RESULT_NAME, onDocRef)),
        getDisplayNameSuppliers(onDocRef, cellDocRef)))
            .orElseGet(onDocRef::getName);
  }

  /**
   * extension point: override to provide different display name suppliers
   */
  protected Stream<Supplier<Optional<String>>> getDisplayNameSuppliers(
      DocumentReference onDocRef, DocumentReference cellDocRef) {
    return Stream.of(() -> displayTitle(onDocRef));
  }

  protected final Optional<String> displayTitle(DocumentReference onDocRef) {
    return Optional.ofNullable(onDocRef)
        .map(docRef -> modelAccess.getOrCreateDocument(docRef, context.getLanguage().orElse("")))
        .map(doc -> doc.getTitle().trim())
        .filter(not(Strings::isNullOrEmpty));
  }

  private Optional<String> renderResultFromCell(DocumentReference cellDocRef,
      ClassField<String> field, DocumentReference onDocRef) {
    if (cellDocRef != null) {
      try {
        return XWikiObjectFetcher.on(modelAccess.getOrCreateDocument(cellDocRef))
            .fetchField(field)
            .stream().findFirst()
            .map(rethrowFunction(text -> velocityService.evaluateVelocityText(text, vContext -> {
              vContext.put("resultDocRef", onDocRef);
              return vContext;
            }).trim()))
            .filter(not(String::isEmpty));
      } catch (XWikiVelocityException exc) {
        log.warn("renderResultFromCell - failed building json for cell [{}] and doc [{}]",
            cellDocRef, onDocRef, exc);
      }
    }
    return Optional.empty();
  }

  @Override
  public Optional<DocumentReference> getSelectedValue(DocumentReference cellDocRef) {
    return Optional.ofNullable(cellDocRef)
        .map(modelAccess::getOrCreateDocument)
        .map(this::getValueSuppliers)
        .flatMap(this::findFirstPresent);
  }

  /**
   * extension point: override to provide different value suppliers
   */
  protected Stream<Supplier<Optional<DocumentReference>>> getValueSuppliers(XWikiDocument cellDoc) {
    return Stream.of(
        () -> getValueFromRequest(cellDoc),
        () -> getValueOnDoc(cellDoc),
        () -> getDefaultValue(cellDoc));
  }

  protected final Optional<DocumentReference> getValueFromRequest(XWikiDocument cellDoc) {
    return context.getCurrentDoc().toJavaUtil()
        .flatMap(onDoc -> structEditService.getAttributeName(cellDoc, onDoc))
        .flatMap(name -> context.getRequestParameter(name).toJavaUtil())
        .flatMap(this::resolve);
  }

  protected final Optional<DocumentReference> getValueOnDoc(XWikiDocument cellDoc) {
    return context.getCurrentDoc().toJavaUtil()
        .flatMap(onDoc -> structEditService.getCellValueAsString(cellDoc, onDoc))
        .flatMap(this::resolve);
  }

  protected final Optional<DocumentReference> getDefaultValue(XWikiDocument cellDoc) {
    return XWikiObjectFetcher.on(cellDoc)
        .fetchField(OptionTagEditorClass.FIELD_VALUE)
        .stream().findFirst()
        .flatMap(this::resolve);
  }

  protected final Optional<DocumentReference> resolve(String fullName) {
    try {
      return Optional.of(modelUtils.resolveRef(fullName, DocumentReference.class));
    } catch (IllegalArgumentException exc) {
      log.debug("unable to resolve ref: {}", exc.getMessage(), exc);
      return Optional.empty();
    }
  }

  private <T> Optional<T> findFirstPresent(Stream<Supplier<Optional<T>>> suppliers) {
    return suppliers.map(Supplier::get)
        .filter(Optional::isPresent).map(Optional::get)
        .findFirst();
  }

}
