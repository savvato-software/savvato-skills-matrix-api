package com.savvato.skillsmatrix.services;

import java.io.IOException;

public interface PictureService {

    String transformFilenameUsingSizeInfo(String photoSize, String filename);
    void writeThumbnailFromOriginal(String resourceType, String filename) throws IOException;
}
