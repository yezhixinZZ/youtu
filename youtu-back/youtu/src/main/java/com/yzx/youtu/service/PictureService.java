package com.yzx.youtu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yzx.youtu.model.dto.picture.PictureQueryRequest;
import com.yzx.youtu.model.dto.picture.PictureReviewRequest;
import com.yzx.youtu.model.dto.picture.PictureUploadByBatchRequest;
import com.yzx.youtu.model.dto.picture.PictureUploadRequest;
import com.yzx.youtu.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yzx.youtu.model.entity.User;
import com.yzx.youtu.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author Merer
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2024-12-17 19:49:39
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 获取查询对象
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片包装类
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 分页获取图片封装类
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验数据
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

}
