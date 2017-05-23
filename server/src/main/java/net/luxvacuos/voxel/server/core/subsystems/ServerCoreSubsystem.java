/*
 * This file is part of Voxel
 * 
 * Copyright (C) 2016-2017 Lux Vacuos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.luxvacuos.voxel.server.core.subsystems;

import java.io.File;

import net.luxvacuos.voxel.server.bootstrap.Bootstrap;
import net.luxvacuos.voxel.server.core.ServerGameSettings;
import net.luxvacuos.voxel.universal.core.subsystems.CoreSubsystem;
import net.luxvacuos.voxel.universal.util.registry.Key;

public class ServerCoreSubsystem extends CoreSubsystem {

	@Override
	public void init() {
		super.init();
		REGISTRY.register(new Key("/Voxel/Settings/World/directory"), Bootstrap.getPrefix() + "/");
		gameSettings = new ServerGameSettings();
		gameSettings.read();
		REGISTRY.load(new File((String) REGISTRY.getRegistryItem(new Key("/Voxel/Settings/file"))));
		REGISTRY.save();
		LANG.load("assets/" + CoreSubsystem.REGISTRY.getRegistryItem(new Key("/Voxel/Settings/Graphics/assets"))
				+ "/langs/" + REGISTRY.getRegistryItem(new Key("/Voxel/Settings/Regional/lang")) + ".json");
	}

}
