package com.rabbitq.entity;

import com.beust.jcommander.Parameter;

public class TargetOptionsEntity {

    @Parameter(names = {"--input","-i"},
            description = "已确定子域",
            required = true)
    private String input;

    @Parameter(names = {"--wordlist","-w"},
            description = "用于更改子域的单词列表")
    private String wordlist="words.txt";

    @Parameter(names = {"--output","-o"},
            description = "结果的输出位置")
    private String output;

    @Parameter(names = {"help", "--help"},
            description = "查看帮助信息",
            help = true)
    private boolean help;

    public boolean isHelp() {
        return help;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getWordlist() {
        return wordlist;
    }

    public void setWordlist(String wordlist) {
        this.wordlist = wordlist;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }
}
