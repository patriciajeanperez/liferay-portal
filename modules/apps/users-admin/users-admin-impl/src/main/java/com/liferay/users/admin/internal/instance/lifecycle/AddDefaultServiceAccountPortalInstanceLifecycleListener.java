/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.users.admin.internal.instance.lifecycle;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.instance.lifecycle.BasePortalInstanceLifecycleListener;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserConstants;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.module.framework.ModuleServiceLifecycle;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.search.spi.model.registrar.ModelSearchConfigurator;
import com.liferay.portal.util.PropsValues;
import com.liferay.users.admin.constants.UsersAdminPortletKeys;

import java.util.Calendar;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Pei-Jung Lan
 */
@Component(service = PortalInstanceLifecycleListener.class)
public class AddDefaultServiceAccountPortalInstanceLifecycleListener
	extends BasePortalInstanceLifecycleListener {

	@Override
	public void portalInstanceRegistered(Company company) throws Exception {
		User defaultServiceAccountUser = _userLocalService.fetchDefaultUser(
			company.getCompanyId(), UserConstants.TYPE_SERVICE_ACCOUNT);

		if (defaultServiceAccountUser != null) {
			return;
		}

		Role adminRole = _roleLocalService.getRole(
			company.getCompanyId(), RoleConstants.ADMINISTRATOR);

		String userName = "default-service-account";

		defaultServiceAccountUser = _userLocalService.addUser(
			UserConstants.USER_ID_DEFAULT, company.getCompanyId(), false,
			PropsValues.DEFAULT_ADMIN_PASSWORD,
			PropsValues.DEFAULT_ADMIN_PASSWORD, false, userName,
			userName + StringPool.AT + company.getMx(),
			LocaleUtil.fromLanguageId(PropsValues.COMPANY_DEFAULT_LOCALE),
			userName, StringPool.BLANK, userName, 0, 0, true, Calendar.JANUARY,
			1, 1970, StringPool.BLANK, UserConstants.TYPE_SERVICE_ACCOUNT, null,
			null, new long[] {adminRole.getRoleId()}, null, false,
			new ServiceContext());

		defaultServiceAccountUser.setDefaultUser(true);

		_userLocalService.updateUser(defaultServiceAccountUser);

		_userLocalService.updateEmailAddressVerified(
			defaultServiceAccountUser.getUserId(), true);

		_indexer.reindex(defaultServiceAccountUser);
	}

	@Reference(
		target = "(indexer.class.name=com.liferay.portal.kernel.model.User)"
	)
	private Indexer<User> _indexer;

	@Reference(
		target = "(indexer.class.name=com.liferay.portal.kernel.model.User)"
	)
	private ModelSearchConfigurator _modelSearchConfigurator;

	@Reference(target = ModuleServiceLifecycle.PORTLETS_INITIALIZED)
	private ModuleServiceLifecycle _moduleServiceLifecycle;

	@Reference(
		target = "(javax.portlet.name=" + UsersAdminPortletKeys.USERS_ADMIN + ")"
	)
	private Portlet _portlet;

	@Reference
	private RoleLocalService _roleLocalService;

	@Reference
	private UserLocalService _userLocalService;

}