package com.savvato.skillsmatrix.utils;

import com.savvato.skillsmatrix.entities.PermIdEntity;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Date;

public class PermIdEntityUtils {
    public static void setPermId(PermIdEntity entity) {
        if (entity.getName() == null) {
            throw new IllegalArgumentException("PermIdEntityUtils.setPermId: entity.getName() is null");
        }

        Date date = new Date();
        String permId = DigestUtils.sha256Hex(entity.getName()) + "-" + date.getTime();
        permId = permId.substring(0, 12);

        entity.setPermId(permId);
    }
}
