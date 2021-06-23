package com.plantation.trancell;

public interface ICommonUi {
    /**
     * Binds the generic textViews, Buttons, Layouts and ListViews in the
     * Managers.
     */
    void bindViews();

    /**
     * used to set handlers and adapters
     */
    void setAdapters();

    /**
     * used to set listeners
     */
    void setListeners();

    void uiInvalidateBtnState();
}