package org.aperteworkflow.files.controller;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.aperteworkflow.files.IFilesRepositoryFacade;
import org.aperteworkflow.files.exceptions.DeleteFileException;
import org.aperteworkflow.files.model.FilesRepositoryItem;
import org.aperteworkflow.files.model.FilesRepositoryItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;


@OsgiController(name = "filescontroller")
public class FilesController implements IOsgiWebController {

    private static Logger logger = Logger.getLogger(FilesController.class.getName());

    private static final String PROCESS_INSTANCE_ID_REQ_PARAM_NAME = "processInstanceId";
    private static final String FILES_REPOSITORY_ITEM_ID_REQ_PARAM_NAME = "filesRepositoryItemId";

    @Autowired
    protected IFilesRepositoryFacade filesRepoFacade;

    @Autowired
    protected IPortalUserSource portalUserSource;

    @ControllerMethod(action = "uploadFile")
    public GenericResultBean uploadFile(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();

        Long uploadedFileId = null;
        HttpServletRequest request = invocation.getRequest();
        try {
            //process only if its multipart content
            if (ServletFileUpload.isMultipartContent(request)) {
                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload();
                // Mandatory parameters for upload

                // Parse the request
                FileItemIterator iter = upload.getItemIterator(request);
                while (iter.hasNext()) {
                    FileItemStream item = iter.next();
                    InputStream stream = item.openStream();
                    if (!item.isFormField()) {
                        String fileName = item.getName();
                        String fileDescription = null;
                        InputStream fileInputStream = stream;
                        Long processInstanceId = getProcessInstanceId(request);
                        String creatorLogin = getCreatorLogin(request);
                        if (processInstanceId != null && fileName != null && fileName.length() > 0 && fileInputStream != null && creatorLogin != null && creatorLogin.length() > 0) {
                            uploadedFileId = filesRepoFacade.uploadFile(fileInputStream, processInstanceId, fileName, fileDescription, creatorLogin);
                            result.setData(uploadedFileId);
                        } else {
                            logger.log(Level.WARNING, "[FILES_REPOSITORY] Not all parameters provided when calling filescontroller.uploadFile. All of [processInstanceId, fileName, fileInputStream, creatorLogin] are required.");
                        }
                    }
                }
            } else {
                logger.log(Level.WARNING, "[FILES_REPOSITORY] For proper file processing request should be multipartcontent");
                throw new RuntimeException("[FILES_REPOSITORY] For proper file processing request should be multipartcontent");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[FILES_REPOSITORY] Cannot get file from request", e);
            result.addError("Cannot get file from request", e.getMessage());
        }
        return result;
    }

    @ControllerMethod(action = "getFilesList")
    public GenericResultBean getFilesList(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();
        HttpServletRequest request = invocation.getRequest();
        Long processInstanceId = getProcessInstanceId(request);
        Collection<FilesRepositoryItem> fileRepoItems = filesRepoFacade.getFilesList(processInstanceId);
        Collection<FilesRepositoryItemDTO> filesRepoItemsDTO = new ArrayList<FilesRepositoryItemDTO>();
        for(FilesRepositoryItem frItem : fileRepoItems) {
            filesRepoItemsDTO.add(new FilesRepositoryItemDTO(frItem));
        }
        result.setData(filesRepoItemsDTO);
        return result;
    }

    @ControllerMethod(action = "deleteFile")
    public GenericResultBean deleteFile(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();
        HttpServletRequest request = invocation.getRequest();
        Long processInstanceId = getProcessInstanceId(request);
        Long filesRepositoryItemId = getFilesRepositoryItemId(request);
        try {
            filesRepoFacade.deleteFile(processInstanceId, filesRepositoryItemId);
            result.setData("File for repo item id=[" + filesRepositoryItemId + "] successfully deleted.");
        } catch (DeleteFileException e) {
            logger.log(Level.SEVERE, "[FILES_REPOSITORY] Cannot delete requested file for repo item id=[" + filesRepositoryItemId + "]", e);
            result.addError("Cannot delete requested file for repo item id=[" + filesRepositoryItemId + "]", e.getMessage());
        }
        return result;
    }

    private Long getFilesRepositoryItemId(HttpServletRequest request) {
        String filesRepositoryItemIdStr = request.getParameter(FILES_REPOSITORY_ITEM_ID_REQ_PARAM_NAME);
        Long filesRepositoryItemId = filesRepositoryItemIdStr != null ? Long.valueOf(filesRepositoryItemIdStr) : null;
        if(filesRepositoryItemId == null) {
            throw new RuntimeException("FilesRepositoryItem ID not provided in request!");
        }
        return filesRepositoryItemId;
    }

    private String getCreatorLogin(HttpServletRequest request) {
        UserData user = portalUserSource.getUserByRequest(request);
        return user.getLogin();
    }

    private Long getProcessInstanceId(HttpServletRequest request) {
        String processInstanceIdStr = request.getParameter(PROCESS_INSTANCE_ID_REQ_PARAM_NAME);
        Long processInstanceId = processInstanceIdStr != null ? Long.valueOf(processInstanceIdStr) : null;
        if(processInstanceId == null) {
            throw new RuntimeException("Process instance ID not provided in request!");
        }
        return processInstanceId;
    }

}