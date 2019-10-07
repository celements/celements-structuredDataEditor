package com.celements.structEditor;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.SpaceReference;

import com.celements.model.context.ModelContext;
import com.celements.model.reference.RefBuilder;
import com.celements.model.util.ModelUtils;
import com.celements.navigation.NavigationConfig;
import com.celements.navigation.factories.JavaNavigationConfigurator;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.service.IPageTypeRole;
import com.celements.struct.StructDataService;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.google.common.base.Strings;

@Component(StructuredDataEditorNavigationConfigurator.CONFIGURATOR_NAME)
public class StructuredDataEditorNavigationConfigurator implements JavaNavigationConfigurator {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      StructuredDataEditorNavigationConfigurator.class);

  public static final String CONFIGURATOR_NAME = "StructuredDataEditor";

  private static final NavigationConfig NAV_CFG = newNavCfgBuilder().fromHierarchyLevel(
      1).toHierarchyLevel(1).cmCssClass("cel_cm_structured_data_editor").build();

  private static NavigationConfig.Builder newNavCfgBuilder() {
    return new NavigationConfig.Builder();
  }

  @Requirement
  private StructDataService structService;

  @Requirement
  private IPageTypeResolverRole pageTypeResolver;

  @Requirement
  private IPageTypeRole pageTypeService;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private ModelContext context;

  @Override
  @NotNull
  public NavigationConfig getNavigationConfig(@NotNull PageTypeReference configReference) {
    LOGGER.debug("getNavigationConfig - for pageTypeRef [{}]", configReference.getConfigName());
    SpaceReference layoutSpaceRef = structService
        .getStructLayoutSpaceRef(context.getCurrentDoc().get())
        .or(this::getCalculatedLayoutSpaceRef);
    LOGGER.info("layout space: [{}]", layoutSpaceRef);
    return NAV_CFG.overlay(newNavCfgBuilder().nodeSpaceRef(layoutSpaceRef).build());
  }

  private SpaceReference getCalculatedLayoutSpaceRef() {
    IPageTypeConfig ptCfg = pageTypeService.getPageTypeConfigForPageTypeRef(
        pageTypeResolver.resolvePageTypeRefForCurrentDoc());
    boolean isStructEdit = !Strings.isNullOrEmpty(ptCfg.getRenderTemplateForRenderMode("edit"));
    String spaceName = ptCfg.getName() + "-" + (isStructEdit ? "EditFields" : "StructData");
    return getInheritedLayoutSpaceRef(modelUtils.resolveRef(spaceName, SpaceReference.class));
  }

  private SpaceReference getInheritedLayoutSpaceRef(SpaceReference configSpaceRef) {
    if (!new PageLayoutCommand().layoutExists(configSpaceRef)) {
      configSpaceRef = RefBuilder.from(configSpaceRef)
          .with(structService.getCentralWikiRef())
          .build(SpaceReference.class);
    }
    return configSpaceRef;
  }

  @Override
  public boolean handles(@NotNull PageTypeReference configReference) {
    return CONFIGURATOR_NAME.equals(configReference.getConfigName());
  }

}
