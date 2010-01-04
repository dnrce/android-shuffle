/*
 * Copyright (C) 2009 Android Shuffle Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dodgybits.android.shuffle.model;

import org.dodgybits.shuffle.dto.ShuffleProtos.Context.Builder;

import android.content.res.Resources;
import android.text.TextUtils;

public class Context implements TracksCompatible {
	public Long id;
	public final String name;
	public final int colourIndex;
	// resource id to icon resource (may be null)
	public final Icon icon;
    public final Long tracksId;
    public final Long tracksModified;

    public Context(Long id, String name, int colourIndex, Icon icon, Long tracksId, Long tracksModified) {
		this.id = id;
		this.name = name;
		this.colourIndex = colourIndex;
		this.icon = icon;
        this.tracksId = tracksId;
        this.tracksModified = tracksModified;
    }
	
	public Context(String name, int colour, Icon icon, Long tracksId, Long tracksModified) {
		this(null, name, colour, icon, tracksId, tracksModified);
	}

    @Override
    public Long getTracksId() {
        return tracksId;
    }

    @Override
    public Long getTracksModified() {
        return tracksModified;
    }

    @Override
    public String getLocalName() {
        return name;
    }

    public static class Icon {
		private static final String cPackage = "org.dodgybits.android.shuffle"; 
		private static final String cType = "drawable";
		
		public static final Icon NONE = new Icon(null, 0, 0);
		
		public final String iconName;
		public final int largeIconId;
		public final int smallIconId;
		
		private Icon(String iconName, int largeIconId, int smallIconId) {
			this.iconName = iconName;
			this.largeIconId = largeIconId;
			this.smallIconId = smallIconId;
		}
		
		public static Icon createIcon(String iconName, Resources res) {
			if (TextUtils.isEmpty(iconName)) return NONE;
			int largeId = res.getIdentifier(iconName, cType, cPackage);
			int smallId = res.getIdentifier(iconName + "_small", cType, cPackage);
			return new Icon(iconName, largeId, smallId);
		}
	}

	public org.dodgybits.shuffle.dto.ShuffleProtos.Context toDto() {
		Builder builder = org.dodgybits.shuffle.dto.ShuffleProtos.Context.newBuilder();
		builder
			.setId(id)
			.setName(name)
			.setColourIndex(colourIndex);
		if (icon != Icon.NONE) {
			builder.setIcon(icon.iconName);
		}
		return builder.build();
	}

	public static Context buildFromDto(
			org.dodgybits.shuffle.dto.ShuffleProtos.Context dto,
			Resources res) {
		Icon icon;
		if (dto.hasIcon()) {
			icon = Icon.createIcon(dto.getIcon(), res);
		} else {
			icon = Icon.NONE;
		}

		return new Context(
				dto.getId(),
				dto.getName(),
				dto.getColourIndex(),
				icon,
				null,
				null);
	}
}
