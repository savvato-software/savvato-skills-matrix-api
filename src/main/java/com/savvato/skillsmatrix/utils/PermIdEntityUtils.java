package com.savvato.skillsmatrix.utils;

import com.savvato.skillsmatrix.entities.PermIdEntityBehavior;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Date;

public class PermIdEntityUtils {
    public static void setId(PermIdEntityBehavior entity) {
        if (entity.getName() == null) {
            throw new IllegalArgumentException("PermIdEntityUtils.setPermId: entity.getName() is null");
        }

        entity.setId(getId(entity.getName()));
    }

    public static String getId(String str) {
        Date date = new Date();
        String id = DigestUtils.sha256Hex(str + "-" + date.getTime());
        return id.substring(0, 12);
    }
}
