package com.nuexpert.schd.web;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang3.StringUtils;

import com.nuexpert.schd.vo.Service;

@FacesConverter("schd.serviceConverter")
public class ServiceConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent comp, String value) {
		if (StringUtils.isNotBlank(value)){
			ScheduleController scheduleController = (ScheduleController) context.getExternalContext().getApplicationMap().get("scheduleController");
			
			if (null!=scheduleController){
				//return scheduleController.getServiceByName(value);
			}
		}
		
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent comp, Object object) {
		
		if (object instanceof Service){
			return ((Service)object).getServiceName();
		}
		
		return null;
	}

}
