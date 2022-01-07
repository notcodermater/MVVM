package com.aspire.baselibrary.serialport.message;


import com.aspire.baselibrary.serialport.TimeUtil;

/**
 * 收到的日志
 */

public  class RecvMessageWenshidu implements IMessage {

    private String command;
    private String message;

    public RecvMessageWenshidu(String command) {
        this.command = command;
        this.message = TimeUtil.currentTime() + "    收到命令：" + command;
    }

    @Override
    public String getMessage() {
        return message;
    }
    public String getCommand() {
        return command;
    }

    @Override
    public boolean isToSend() {
        return false;
    }
}
