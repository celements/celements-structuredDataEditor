package com.celements.structEditor;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.reference.RefBuilder;
import com.celements.model.util.ModelUtils;
import com.celements.navigation.NavigationConfig;
import com.celements.navigation.factories.JavaNavigationConfigurator;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.classes.PageTypeClass;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.service.IPageTypeRole;
import com.celements.web.plugin.cmd.PageLayoutCommand;
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
    SpaceReference configSpaceRef = getConfigSpaceRefFromPageTypeLayout()
        .orElseGet(this::getCalculatedConfigSpaceRef);
    LOGGER.info("configSpace: [{}]", configSpaceRef);
    return NAV_CFG.overlay(newNavCfgBuilder().nodeSpaceRef(configSpaceRef).build());
  }

  private Optional<SpaceReference> getConfigSpaceRefFromPageTypeLayout() {
    String spaceName = XWikiObjectFetcher.on(context.getCurrentDoc().get())
        .fetchField(PageTypeClass.PAGE_LAYOUT).first().or("");
    if (!spaceName.isEmpty()) {
      SpaceReference configSpaceRef = getInheritedConfigSpaceRef(spaceName);
      if (new PageLayoutCommand().layoutExists(configSpaceRef)) {
        return Optional.of(configSpaceRef);
      }
    }
    return Optional.empty();
  }

  private SpaceReference getCalculatedConfigSpaceRef() {
    IPageTypeConfig ptCfg = pageTypeService.getPageTypeConfigForPageTypeRef(
        pageTypeResolver.resolvePageTypeRefForCurrentDoc());
    boolean isStructEdit = !Strings.isNullOrEmpty(ptCfg.getRenderTemplateForRenderMode("edit"));
    String spaceName = ptCfg.getName() + "-" + (isStructEdit ? "EditFields" : "StructData");
    return getInheritedConfigSpaceRef(spaceName);
  }

  private SpaceReference getInheritedConfigSpaceRef(String spaceName) {
    SpaceReference configSpaceRef = modelUtils.resolveRef(spaceName, SpaceReference.class);
    if (!new PageLayoutCommand().layoutExists(configSpaceRef)) {
      configSpaceRef = RefBuilder.from(configSpaceRef).with(getCentralWikiRef())
          .build(SpaceReference.class);
    }
    return configSpaceRef;
  }

  private WikiReference getCentralWikiRef() {
    return new WikiReference("celements2web");
  }

  @Override
  public boolean handles(@NotNull PageTypeReference configReference) {
    return CONFIGURATOR_NAME.equals(configReference.getConfigName());
  }

}
