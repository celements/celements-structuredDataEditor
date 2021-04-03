package com.celements.struct.edit.autocomplete;

import static com.celements.common.MoreObjectsCel.*;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.util.ModelUtils;
import com.celements.sajson.JsonBuilder;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.web.IWebSearchService;
import com.celements.structEditor.SelectAutocompleteRole;
import com.celements.structEditor.StructuredDataEditorService;
import com.celements.structEditor.classes.OptionTagEditorClass;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class DefaultSelectAutocomplete implements SelectAutocompleteRole {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  @Requirement
  private StructuredDataEditorService structEditService;

  @Requirement
  private IWebSearchService webSearchService;

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
    return "";
  }

  @Override
  public @NotNull LuceneSearchResult search(DocumentReference cellDocRef, String searchTerm) {
    return webSearchService.webSearch(searchTerm, (cellDocRef == null) ? null
        : modelAccess.getOrCreateDocument(cellDocRef));
  }

  @Override
  public JsonBuilder getJsonForValue(DocumentReference valueDocRef) {
    JsonBuilder json = new JsonBuilder();
    json.openDictionary();
    json.addProperty("fullName", modelUtils.serializeRef(valueDocRef));
    json.addProperty("name", displayNameForValue(valueDocRef));
    json.closeDictionary();
    return json;
  }

  @Override
  public String displayNameForValue(DocumentReference valueDocRef) {
    String lang = context.getLanguage().orElse("");
    return asOptNonBlank(modelAccess.getOrCreateDocument(valueDocRef, lang).getTitle())
        .orElseGet(() -> modelUtils.serializeRefLocal(valueDocRef));
  }

  @Override
  public Optional<DocumentReference> getSelectedValue(DocumentReference cellDocRef) {
    XWikiDocument cellDoc = modelAccess.getOrCreateDocument(cellDocRef);
    return getValueResolvers(cellDoc)
        .map(Supplier::get).filter(Optional::isPresent).map(Optional::get)
        .findFirst()
        .flatMap(this::resolve);
  }

  protected Stream<Supplier<Optional<String>>> getValueResolvers(XWikiDocument cellDoc) {
    return Stream.of(
        () -> getValueFromRequest(cellDoc),
        () -> getValueOnDoc(cellDoc),
        () -> getDefaultValue(cellDoc));
  }

  protected final Optional<String> getValueFromRequest(XWikiDocument cellDoc) {
    return context.getCurrentDoc().toJavaUtil()
        .flatMap(onDoc -> structEditService.getAttributeName(cellDoc, onDoc))
        .flatMap(name -> context.getRequestParameter(name).toJavaUtil());
  }

  protected final Optional<String> getValueOnDoc(XWikiDocument cellDoc) {
    return context.getCurrentDoc().toJavaUtil()
        .flatMap(onDoc -> structEditService.getCellValueAsString(cellDoc, onDoc));
  }

  protected final Optional<String> getDefaultValue(XWikiDocument cellDoc) {
    return XWikiObjectFetcher.on(cellDoc)
        .fetchField(OptionTagEditorClass.FIELD_VALUE)
        .stream().findFirst();
  }

  private Optional<DocumentReference> resolve(String fullName) {
    try {
      return Optional.of(modelUtils.resolveRef(fullName, DocumentReference.class));
    } catch (IllegalArgumentException exc) {
      log.debug("unable to resolve ref: {}", exc.getMessage(), exc);
      return Optional.empty();
    }
  }

}
