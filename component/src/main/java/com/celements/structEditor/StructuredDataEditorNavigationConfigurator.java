package com.celements.structEditor;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.SpaceReference;

import com.celements.model.context.ModelContext;
import com.celements.navigation.NavigationConfig;
import com.celements.navigation.NavigationConfig.Builder;
import com.celements.navigation.factories.JavaNavigationConfigurator;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;

@Component(StructuredDataEditorNavigationConfigurator.CONFIGURATOR_NAME)
public class StructuredDataEditorNavigationConfigurator implements JavaNavigationConfigurator {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      StructuredDataEditorNavigationConfigurator.class);

  public static final String CONFIGURATOR_NAME = "StructuredDataEditor";

  private final static NavigationConfig defNavConfig;
  static {
    Builder b = new NavigationConfig.Builder();
    b.fromHierarchyLevel(1);
    b.toHierarchyLevel(1);
    b.cmCssClass("cel_cm_structured_data_editor");
    defNavConfig = b.build();
  };

  @Requirement
  IPageTypeResolverRole pageTypeResolver;

  @Requirement
  ModelContext modelContext;

  @Override
  @NotNull
  public NavigationConfig getNavigationConfig(@NotNull PageTypeReference configReference) {
    LOGGER.debug("getNavigationConfig: for pageTypeRef '{}'", configReference.getConfigName());
    String spaceName = pageTypeResolver.getPageTypeRefForCurrentDoc().getConfigName();
    SpaceReference editorConfigSpace = new SpaceReference(spaceName + "-EditFields",
        modelContext.getWikiRef());
    Builder b = new NavigationConfig.Builder();
    b.nodeSpaceRef(editorConfigSpace);
    return defNavConfig.overlay(b.build());
  }

  @Override
  public boolean handles(@NotNull PageTypeReference configReference) {
    return CONFIGURATOR_NAME.equals(configReference.getConfigName());
  }

}
