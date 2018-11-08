package com.pearnode.common.ftp;

import com.pearnode.constants.FixedValuesRegistry;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by USER on 2/21/2018.
 */
public class DocumentFTPUploader {

    private String docName = null;
    private File docFile = null;

    public DocumentFTPUploader(String docName, File docFile){
        this.docName = docName;
        this.docFile = docFile;
    }

    public String doUpload(){
        String uploadFilePath = null;
        try {
            String rootDir = "/resources";
            String appsDir = rootDir + "/apps/" + FixedValuesRegistry.APP_NAME + "/";
            String documentsDirectory = appsDir + "/documents/";

            FTPClient ftpClient = new FTPClient();
            ftpClient.setDefaultTimeout(5 * 60 * 1000); // 5 mins

            ftpClient.connect(FixedValuesRegistry.FTP_HOST, 12996);
            ftpClient.login("pnftp", "pnftp987126");

            ftpCreateDirectoryTree(ftpClient, documentsDirectory);
            ftpClient.changeWorkingDirectory(documentsDirectory);

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(docFile));
            uploadFilePath = documentsDirectory + docName;
            try{
                ftpClient.changeWorkingDirectory(documentsDirectory);
                ftpClient.deleteFile(docName);
                ftpClient.storeFile(docName, buffIn);
            }catch (Exception e){
                e.printStackTrace();
            }
            buffIn.close();

            ftpClient.logout();
            ftpClient.disconnect();
        }catch (Exception e){
            e.printStackTrace();
        }
        return uploadFilePath;
    }

    /**
     * utility to create an arbitrary directory hierarchy on the remote ftp server
     * @param client
     * @param dirTree  the directory tree only delimited with / chars.  No file name!
     * @throws Exception
     */
    private static void ftpCreateDirectoryTree( FTPClient client, String dirTree ) throws IOException {

        boolean dirExists = true;

        //tokenize the string and attempt to change into each directory level.  If you cannot, then start creating.
        String[] directories = dirTree.split("/");
        for (String dir : directories ) {
            if (!dir.isEmpty() ) {
                dirExists = client.changeWorkingDirectory(dir);
                if (!dirExists) {
                    if (!client.makeDirectory(dir)) {
                        throw new IOException("Unable to create remote directory '" + dir + "'.  error='" + client.getReplyString()+"'");
                    }
                    if (!client.changeWorkingDirectory(dir)) {
                        throw new IOException("Unable to change into newly created remote directory '" + dir + "'.  error='" + client.getReplyString()+"'");
                    }
                }
            }
        }
    }
}
