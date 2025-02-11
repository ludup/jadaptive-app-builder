package com.jadaptive.api.ui.editor;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.TableView;

@ObjectDefinition(resourceKey = ContentEdit.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION, defaultColumn = "name")
@TableView(defaultColumns = { "name" })
//@PageMenu(bundle = UserInterfaceService.BUNDLE, feature = HtmlContentService.DEVELOPER_TOOLS, icon = "fa-wrench", i18n = "contentEditor.name", parent = ApplicationMenuService.CUSTOMIZE_MENU, withPermission  = "tenant.read")
public class ContentEdit extends AbstractUUIDEntity {

	private static final long serialVersionUID = -6325538173424390831L;
	public static final String RESOURCE_KEY = "contentEdit";
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

}
