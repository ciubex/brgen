/**
 * This file is part of BRG application.
 * 
 * Copyright (C) 2014 Claudiu Ciobotariu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ro.ciubex.brgen.form;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * This is a workaround for the clickable image view.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class ClickableImageView extends ImageView {

	/**
	 * Nothing special just call super constructor.
	 * 
	 * @param context
	 */
	public ClickableImageView(Context context) {
		super(context);
	}

	/**
	 * Nothing special just call super constructor.
	 * 
	 * @param context
	 * @param attrs
	 */
	public ClickableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Nothing special just call super constructor.
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public ClickableImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#setPressed(boolean)
	 */
	@Override
	public void setPressed(boolean pressed) {
		if (pressed && getParent() instanceof View
				&& ((View) getParent()).isPressed()) {
			return;
		}
		super.setPressed(pressed);
	}

}
