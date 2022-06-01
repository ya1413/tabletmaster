package com.sxdzsoft.easyresource.serviceImple;

import com.sxdzsoft.easyresource.domain.*;
import com.sxdzsoft.easyresource.mapper.*;
import com.sxdzsoft.easyresource.service.MyFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @ClassName MyFileServiceImple
 * @Description TODO
 * @Author wujian
 * @Date 2022/5/31 15:29
 * @Version 1.0
 **/
@Service
public class MyFileServiceImple implements MyFileService {
    @Autowired
    private MyFormItemMapper myFormItemMapper;
    @Autowired
    private MyFileMapper myFileMapper;
    @Autowired
    private MyFormMapper myFormMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MyDirMapper myDirMapper;
    @Override
    @Transactional
    public int addFormFile(int fileType,long fileSize,int itemId, String store, String orgname, User owner) {
        MyFormItem item=this.myFormItemMapper.getById(itemId);
        MyFile myFile=new MyFile();
        myFile.setName(orgname);
        myFile.setIsUse(1);
        MyDir myDir=item.getStoreDir();
        myFile.setMyDir(myDir);
        myFile.setStore(store);
        User u=this.userMapper.getById(owner.getId());
        myFile.setOwner(u);
        myFile.setMyFormItem(item);
        myDir.setChild_file_total(myDir.getChild_file_total()+1);
        myFile.setType(fileType);
        myFile.setSize(fileSize);
        int limit=item.getMount_limit();
        List<MyFile> files=this.myFileMapper.queryByMyFormItemIdIsAndIsUseIs(item.getId(),1);
        if(files!=null&&files.size()+1>=limit){
            item.setStatu(1);
        }
        if(files!=null&&files.size()+1<limit){
            item.setStatu(2);
        }
        this.myFormItemMapper.save(item);
        this.myDirMapper.save(myDir);
        this.myFileMapper.save(myFile);
        return HttpResponseRebackCode.Ok;
    }

    @Override
    public MyFile queryFileById(int fileId) {
        return this.myFileMapper.getById(fileId);
    }

    @Override
    public List<MyFile> queryByMyDirIdIs(int dirId,int isUse) {
        return this.myFileMapper.queryByMyDirIdIsAndIsUseIs(dirId,isUse);
    }

    @Override
    @Transactional
    public int delFormFile(int fileId) {
        MyFile file=this.myFileMapper.getById(fileId);
        MyDir myDir=file.getMyDir();
        myDir.setChild_file_total(myDir.getChild_file_total()-1);
        this.myDirMapper.save(myDir);
        MyFormItem myFormItem=file.getMyFormItem();
        int limit=myFormItem.getMount_limit();
        int currentFiles=this.myFileMapper.queryByMyFormItemIdIsAndIsUseIs(myFormItem.getId(),1).size();
        if(currentFiles-1<=0){
            myFormItem.setStatu(0);
        }
        else if(currentFiles-1<limit){
            myFormItem.setStatu(2);
        }
        this.myFormItemMapper.save(myFormItem);
        file.setIsUse(0);
        this.myFileMapper.save(file);
        return HttpResponseRebackCode.Ok;
    }
}
