package org.aperteworkflow.files.dao;

import org.aperteworkflow.files.model.FilesRepositoryProcessAttribute;
import org.aperteworkflow.files.model.IFilesRepositoryAttribute;

/**
 * Created by pkuciapski on 2014-05-13.
 */
public class FilesRepositoryProcessAttributeFactoryImpl extends FilesRepositoryAttributeFactory {

    @Override
    public IFilesRepositoryAttribute create() {
        return new FilesRepositoryProcessAttribute();
    }
}