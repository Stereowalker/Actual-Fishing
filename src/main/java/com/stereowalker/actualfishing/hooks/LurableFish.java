package com.stereowalker.actualfishing.hooks;

public interface LurableFish {
	public LurableFish.LureState lureDecision();
	public void setWillBite(LurableFish.LureState bite);
    
    public enum LureState {
    	NOT_DECIDED, WILL_IGNORE_HOOK, 
    	WILL_APPROACH_BUT_NOT_BITE, WILL_BITE
    }
}
