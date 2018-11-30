package com.celements.structEditor;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.SpaceReference;

import com.celements.model.context.ModelContext;
import com.celements.navigation.NavigationConfig;
import com.celements.navigation.factories.JavaNavigationConfigurator;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeRole;
import com.google.common.base.Strings;

@Component(StructuredDataEditorNavigationConfigurator.CONFIGURATOR_NAME)
public class StructuredDataEditorNavigationConfigurator implements JavaNavigationConfigurator {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      StructuredDataEditorNavigationConfigurator.class);

  public static final String CONFIGURATOR_NAME = "StructuredDataEditor";

  private final static NavigationConfig NAV_CFG = newNavCfgBuilder().fromHierarchyLevel(
      1).toHierarchyLevel(1).cmCssClass("cel_cm_structured_data_editor").build();

  private static NavigationConfig.Builder newNavCfgBuilder() {
    return new NavigationConfig.Builder();
  }

  @Requirement
  private ModelContext context;

  @Requirement
  private IPageTypeRole pageTypeService;

  @Override
  @NotNull
  public NavigationConfig getNavigationConfig(@NotNull PageTypeReference ptRef) {
    LOGGER.debug("getNavigationConfig - for pageTypeRef [{}]", ptRef.getConfigName());
    IPageTypeConfig ptCfg = pageTypeService.getPageTypeConfigForPageTypeRef(ptRef);
    boolean isStructEdit = !Strings.isNullOrEmpty(ptCfg.getRenderTemplateForRenderMode("edit"));
    String spaceName = ptCfg.getName() + "-" + (isStructEdit ? "EditFields" : "StructData");
    LOGGER.info("configSpace: [{}]", spaceName);
    SpaceReference configSpaceRef = new SpaceReference(spaceName, context.getWikiRef());
    return NAV_CFG.overlay(newNavCfgBuilder().nodeSpaceRef(configSpaceRef).build());
  }

  @Override
  public boolean handles(@NotNull PageTypeReference configReference) {
    return CONFIGURATOR_NAME.equals(configReference.getConfigName());
  }

}
