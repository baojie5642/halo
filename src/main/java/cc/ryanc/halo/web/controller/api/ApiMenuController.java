package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.domain.Menu;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.enums.ResponseStatus;
import cc.ryanc.halo.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <pre>
 *     菜单API
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/6/6
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/api/menus")
public class ApiMenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 获取所有菜单
     *
     * <p>
     *     result json:
     *     <pre>
     * {
     *     "code": 200,
     *     "msg": "OK",
     *     "result": [
     *         {
     *             "menuId": ,
     *             "menuName": "",
     *             "menuUrl": "",
     *             "menuSort": ,
     *             "menuIcon": "",
     *             "menuTarget":
     *         }
     *     ]
     * }
     *     </pre>
     * </p>
     *
     * @return JsonResult
     */
    @GetMapping
    public JsonResult menus() {
        List<Menu> menus = menuService.findAll();
        if (null != menus && menus.size() > 0) {
            return new JsonResult(ResponseStatus.SUCCESS.getCode(), ResponseStatus.SUCCESS.getMsg(), menus);
        } else {
            return new JsonResult(ResponseStatus.EMPTY.getCode(), ResponseStatus.EMPTY.getMsg());
        }
    }
}
