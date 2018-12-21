package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.domain.Gallery;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.enums.ResponseStatus;
import cc.ryanc.halo.service.GalleryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 *     图库API
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/6/6
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/api/galleries")
public class ApiGalleryController {

    @Autowired
    private GalleryService galleryService;

    /**
     * 获取所有图片
     *
     * <p>
     *     result json:
     *     <pre>
     * {
     *     "code": 200,
     *     "msg": "OK",
     *     "result": [
     *         {
     *             "galleryId": ,
     *             "galleryName": "",
     *             "galleryDesc": "",
     *             "galleryDate": "",
     *             "galleryLocation": "",
     *             "galleryThumbnailUrl": "",
     *             "galleryUrl": ""
     *         }
     *     ]
     * }
     *     </pre>
     * </p>
     *
     * @return JsonResult
     */
    @GetMapping
    public JsonResult galleries() {
        List<Gallery> galleries = galleryService.findAll();
        if (null != galleries && galleries.size() > 0) {
            return new JsonResult(ResponseStatus.SUCCESS.getCode(), ResponseStatus.SUCCESS.getMsg(), galleries);
        } else {
            return new JsonResult(ResponseStatus.EMPTY.getCode(), ResponseStatus.EMPTY.getMsg());
        }
    }

    /**
     * 获取单张图片的信息
     *
     * <p>
     *     result json:
     *     <pre>
     * {
     *     "code": 200,
     *     "msg": "OK",
     *     "result": [
     *         {
     *             "galleryId": ,
     *             "galleryName": "",
     *             "galleryDesc": "",
     *             "galleryDate": "",
     *             "galleryLocation": "",
     *             "galleryThumbnailUrl": "",
     *             "galleryUrl": ""
     *         }
     *     ]
     * }
     *     </pre>
     * </p>
     *
     * @param id id
     * @return JsonResult
     */
    @GetMapping(value = "/{id}")
    public JsonResult galleries(@PathVariable("id") Long id) {
        Optional<Gallery> gallery = galleryService.findByGalleryId(id);
        if (gallery.isPresent()) {
            return new JsonResult(ResponseStatus.SUCCESS.getCode(), ResponseStatus.SUCCESS.getMsg(), gallery.get());
        } else {
            return new JsonResult(ResponseStatus.NOTFOUND.getCode(), ResponseStatus.NOTFOUND.getMsg());
        }
    }
}
