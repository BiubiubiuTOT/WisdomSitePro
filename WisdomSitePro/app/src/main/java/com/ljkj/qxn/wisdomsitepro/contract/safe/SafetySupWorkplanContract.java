package com.ljkj.qxn.wisdomsitepro.contract.safe;

import com.ljkj.qxn.wisdomsitepro.data.entity.PageInfo;
import com.ljkj.qxn.wisdomsitepro.data.entity.SafeEduInfo;
import com.ljkj.qxn.wisdomsitepro.data.entity.SafetySupWorkplanInfo;
import com.ljkj.qxn.wisdomsitepro.model.SafeModel;

import cdsp.android.presenter.BasePresenter;
import cdsp.android.ui.base.BaseView;

/**
 * 类描述：安全监督方案
 * 创建人：mhl
 * 创建时间：2018/3/13 13:56
 * 修改人：
 * 修改时间：
 * 修改备注：
 */

public interface SafetySupWorkplanContract {

    interface View extends BaseView {

        void showSafetySupWorkplan(SafetySupWorkplanInfo data);
    }

    abstract class Presenter extends BasePresenter<View, SafeModel> {
        public Presenter(View view, SafeModel model) {
            super(view, model);
        }

        public abstract void viewSafetySupWorkplan(String proId);
    }
}
