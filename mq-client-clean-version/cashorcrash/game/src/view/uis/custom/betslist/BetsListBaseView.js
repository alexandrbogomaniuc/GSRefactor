import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';

class BetsListBaseView extends SimpleUIView
{   
    updateLayout(aLayout_rt, aIsPortraitMode_bl)
    {
        this._fContentX_num = aLayout_rt.x;
        this._fContentY_num = aLayout_rt.y;

        this._updateViewPosition(aIsPortraitMode_bl);

        if (this._fContentWidth_num !== aLayout_rt.width || this._fContentHeight_num !== aLayout_rt.height || this._fIsPortraitMode_bl !== aIsPortraitMode_bl)
        {
            this._fContentWidth_num = aLayout_rt.width;
            this._fContentHeight_num = aLayout_rt.height;
            this._fIsPortraitMode_bl = aIsPortraitMode_bl;

            if (this.uiInfo) // view is already initialized
            {
                this._updateLayoutSettings();
            }
        }
    }

    _updateViewPosition(aIsPortraitMode_bl)
    {
        let lX_num = this._fContentX_num || 0;
        let lY_num = this._fContentY_num || 0;

        this.position.set(lX_num, lY_num);
    }

    //INIT...
    constructor()
    {
        super();

        this._fContentWidth_num = undefined;
        this._fContentHeight_num = undefined;
        this._fContentX_num = undefined;
        this._fContentY_num = undefined;
        this._fIsPortraitMode_bl = false;
    }
    
    __init()
    {
        super.__init();
    }
    //...INIT

    _updateLayoutSettings()
    {
        if (this._fContentWidth_num === undefined || this._fContentHeight_num === undefined) return;
    }
}

export default BetsListBaseView;