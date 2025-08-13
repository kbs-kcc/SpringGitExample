package com.example.myapp.board.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = "fileData")
public class BoardUploadFile {
    private int fileId;              
    private int boardId;            
    private String fileName;        
    private long fileSize;          
    private String fileContentType; 
    private byte[] fileData;        
}
