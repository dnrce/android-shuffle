package org.dodgybits.shuffle.android.core.model.protocol;

import org.dodgybits.shuffle.android.core.model.Context;
import org.dodgybits.shuffle.android.core.model.Id;
import org.dodgybits.shuffle.dto.ShuffleProtos.Context.Builder;

public class ContextProtocolTranslator  implements EntityProtocolTranslator<Context , org.dodgybits.shuffle.dto.ShuffleProtos.Context>{

    public org.dodgybits.shuffle.dto.ShuffleProtos.Context toMessage(Context context) {
        Builder builder = org.dodgybits.shuffle.dto.ShuffleProtos.Context.newBuilder();
        builder
            .setId(context.getLocalId().getId())
            .setGaeEntityId(context.getGaeId().getId())
            .setName((context.getName()))
            .setModified(ProtocolUtil.toDate(context.getModifiedDate()))
            .setColourIndex(context.getColourIndex())
            .setActive(context.isActive())
            .setDeleted(context.isDeleted());
        
        final String iconName = context.getIconName();
        if (iconName != null) {
            builder.setIcon(iconName);
        }
        
        return builder.build();
    }

    public Context fromMessage(
            org.dodgybits.shuffle.dto.ShuffleProtos.Context dto) {
        Context.Builder builder = Context.newBuilder();
        builder
            .setLocalId(Id.create(dto.getId()))
            .setName(dto.getName())
            .setModifiedDate(ProtocolUtil.fromDate(dto.getModified()))
            .setColourIndex(dto.getColourIndex());

        if (dto.hasGaeEntityId()) {
            builder.setGaeId(Id.create(dto.getGaeEntityId()));
        }

        if (dto.hasActive()) {
            builder.setActive(dto.getActive());
        } else {
            builder.setActive(true);
        }

        if (dto.hasDeleted()) {
            builder.setDeleted(dto.getDeleted());
        } else {
            builder.setDeleted(false);
        }

        if (dto.hasIcon()) {
            builder.setIconName(dto.getIcon());
        }

        return builder.build();
    }      

}
