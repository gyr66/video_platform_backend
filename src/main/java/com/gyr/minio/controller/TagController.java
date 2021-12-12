package com.gyr.minio.controller;

import com.gyr.minio.bean.Message;
import com.gyr.minio.bean.Tag;
import com.gyr.minio.mapper.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasRole('admin')")
@RequestMapping("/tag")
public class TagController {
    @Autowired
    TagMapper tagMapper;

    @GetMapping("/list")
    public List<Tag> list() {
        return tagMapper.getAll();
    }

    @GetMapping("/add")
    public Message add(String name) {
        Tag tag = new Tag(-1, name);
        tagMapper.addTag(tag);
        return Message.success("新增成功!").put("tag", tag);
    }

    @DeleteMapping("/delete/{id}")
    public Message delete(@PathVariable("id") int id) {
        tagMapper.removeTag(id);
        return Message.success("删除成功!");
    }
}
