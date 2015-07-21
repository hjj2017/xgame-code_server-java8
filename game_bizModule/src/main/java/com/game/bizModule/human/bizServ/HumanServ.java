package com.game.bizModule.human.bizServ;

import com.game.bizModule.human.Human;
import com.game.bizModule.human.HumanLog;
import com.game.bizModule.human.HumanStateTable;
import com.game.bizModule.human.entity.HumanEntryEntity;
import com.game.bizModule.human.event.IHumanEventListen;
import com.game.gameServer.bizServ.AbstractBizServ;
import com.game.part.dao.CommDao;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 玩家角色服务
 *
 * @author hjj2019
 * @since 2015/7/11
 *
 */
public class HumanServ extends AbstractBizServ implements IServ_QueryHumanEntryList, IServ_CreateHuman, IServ_EnterHuman {
    /** 单例对象 */
    public static final HumanServ OBJ = new HumanServ();
    /** 角色时间监听列表 */
    private final List<IHumanEventListen> _ell = new ArrayList<>();
    /** 角色名称集合 */
    public final Set<String> _humanFullNameSet = new HashSet<>();

    /**
     * 类默认构造器
     *
     */
    private HumanServ() {
        super.needToInit(this);
    }

    @Override
    public void init() {
        // 获取角色入口列表
        List<HumanEntryEntity> heel = CommDao.OBJ.getResultList(HumanEntryEntity.class);
        // 添加角色名称到集合
        heel.forEach(hee -> {
            this._humanFullNameSet.add(hee._fullName);
        });
    }

    /**
     * 注册角色事件监听
     *
     * @param newEL
     *
     */
    public void regEventListen(IHumanEventListen newEL) {
        if (newEL == null) {
            // 如果参数对象为空,
            // 则直接退出!
            return;
        }

        // 看看有没有类型相同的事件对象?
        IHumanEventListen oldEL = this._ell.stream().filter(el -> {
            return el != null
                && el.getClass().equals(newEL.getClass());
        }).findFirst().orElse(null);

        if (oldEL != null) {
            // 如果已经注册过监听器,
            // 则直接退出!
            HumanLog.LOG.error(MessageFormat.format(
                "已经注册过程 {0}",
                newEL.getClass().getName()
            ));
            return;
        }

        this._ell.add(newEL);
    }

    /**
     * 触发建角事件
     *
     * @param h
     *
     */
    public void fireCreateHumanEvent(Human h) {
        if (h == null) {
            return;
        } else {
            this._ell.forEach(el -> el.onCreateNew(h));
        }
    }

    /**
     * 触发数据库加载事件
     *
     * @param h
     *
     */
    public void fireLoadDbEvent(Human h) {
        if (h == null) {
            return;
        } else {
            this._ell.forEach(el -> el.onLoadDb(h));
        }
    }

    /**
     * 触发进入游戏事件
     *
     * @param h
     *
     */
    public void fireEntryGameEvent(Human h) {
        if (h == null) {
            return;
        } else {
            this._ell.forEach(el -> el.onEntryGame(h));
        }
    }

    /**
     * 触发退出游戏事件
     *
     * @param h
     *
     */
    public void fireQuitGameEvent(Human h) {
        if (h == null) {
            return;
        }

        // 获取角色状态表
        HumanStateTable hStateTbl = h.getPropValOrCreate(HumanStateTable.class);

        if (hStateTbl._inGame) {
            // 如果玩家处在游戏中状态,
            // 触发退出游戏事件
            this._ell.forEach(el -> el.onQuitGame(h));
        }
    }
}
