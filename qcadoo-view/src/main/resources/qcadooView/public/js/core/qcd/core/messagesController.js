/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.3
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
var QCD = QCD || {};

QCD.MessagesController = function () {
    "use strict";

    if (!(this instanceof QCD.MessagesController)) {
        return new QCD.MessagesController();
    }

	var initialized = false,
        pnotify_stack = {"dir1": "up", "dir2": "left", "firstpos1": 15, "firstpos2": 30};
	
	this.clearMessager = function () {
		$.pnotify_remove_all();
	};
	
	this.addMessage = function (message) { // type = [info|error|success]
		var type = message.type.toLowerCase(),
		    messageOptionsObject = null;
            
		if (typeof message.autoClose === 'undefined' || message.autoClose === null) {
			message.autoClose = true;
		}
		
		if (type === "failure") {
			type = "error";
		}
		
		messageOptionsObject = {
				pnotify_title: message.title,
				pnotify_text: message.content,
				pnotify_stack: pnotify_stack,
				pnotify_history: false,
				pnotify_width: "300px",
				pnotify_type: type,
				pnotify_addclass: type === 'success' ? 'ui-state-success' : '',
				pnotify_notice_icon: type === 'success' ? 'ui-icon ui-icon-success' : 'ui-icon ui-icon-notify',
				pnotify_error_icon: 'ui-icon ui-icon-error',
				pnotify_opacity: 0.9,
				pnotify_delay: 4000,
				pnotify_hide: message.autoClose
			}; 
		
		if (! message.autoClose) {
			messageOptionsObject.pnotify_width = "400px";
			messageOptionsObject.pnotify_addclass = messageOptionsObject.pnotify_addclass + ' noAutoCloseClass';
		}
		
		$.pnotify(messageOptionsObject);
		initialized = true;
	};
	
	this.isInitialized = function () {
		return initialized;
	};

};

