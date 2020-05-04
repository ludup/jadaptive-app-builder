package com.jadaptive.app.ui;

import org.jsoup.select.Elements;

import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.codesmith.webbits.Widget;

@Widget
@View(contentType = "text/html")
@Resource
public class Footer {

    @Out
    public Elements service(@In Elements contents) {
	return contents;
    }
}