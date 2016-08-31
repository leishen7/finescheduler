package com.nuexpert.schd.web;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;

import com.nuexpert.schd.db.HolidayDAO;
import com.nuexpert.schd.db.ServiceBookingDAO;
import com.nuexpert.schd.db.ServiceDAO;
import com.nuexpert.schd.db.UserDAO;
import com.nuexpert.schd.feed.WeatherForcast;
import com.nuexpert.schd.util.EmailUtil;
import com.nuexpert.schd.util.Status;
import com.nuexpert.schd.util.WebUtil;
import com.nuexpert.schd.vo.Appointment;
import com.nuexpert.schd.vo.Holiday;
import com.nuexpert.schd.vo.Service;
import com.nuexpert.schd.vo.ServiceBooking;
import com.nuexpert.schd.vo.ServicePromotion;
import com.nuexpert.schd.vo.User;

@SuppressWarnings("serial")
public abstract class AbstractScheduleController implements Serializable {
	protected static final Logger log = Logger
			.getLogger(AbstractScheduleController.class);
	protected ScheduleModel eventModel;
	// own appointments
	protected Appointment newAppointment = new Appointment();
	protected Appointment existingAppointment = new Appointment();

	protected String userId = null;
	private Service serv;
	private User user = new User();

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	private String currentWeather = "";

	public String getCurrentWeather() {
		return currentWeather;
	}

	public void setCurrentWeather(String currentWeather) {
		this.currentWeather = currentWeather;
	}

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"HH:mm");

	public Service getServ() {
		return serv;
	}

	public void setServ(Service serv) {
		this.serv = serv;
	}

	// appointment from others

	@ManagedProperty("#{serviceDAO}")
	private ServiceDAO serviceDAO;
	@ManagedProperty("#{userDAO}")
	private UserDAO userDAO;

	public UserDAO getUserDAO() {
		return userDAO;
	}

	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

	private HolidayDAO holidayDAO = new HolidayDAO();

	abstract public ServiceBookingDAO getServiceBookingDAO();

	abstract public String getEventTitle(ServiceBooking booking);

	abstract public ServiceBooking createServiceBooking();

	abstract public void onEventSelect(SelectEvent selectEvent);

	public Calendar c = Calendar.getInstance();
	public Calendar c2 = Calendar.getInstance();

	public ScheduleModel getEventModel() {
		return eventModel;
	}

	public void setEventModel(ScheduleModel eventModel) {
		this.eventModel = eventModel;
	}

	public Appointment getNewAppointment() {
		return newAppointment;
	}

	public void setNewAppointment(Appointment newAppointment) {
		this.newAppointment = newAppointment;
	}

	public Appointment getExistingAppointment() {
		return existingAppointment;
	}

	public void setExistingAppointment(Appointment existingAppointment) {
		this.existingAppointment = existingAppointment;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void init() throws Exception {
		eventModel = new DefaultScheduleModel();

		HttpServletRequest request = (HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest();

		userId = request.getRemoteUser();
		if (userId != null) {
			User theUser = userDAO.getUser(userId);
			if (theUser != null)
				setUser(theUser);
		}

		String serviceId = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("serviceId");
		log.debug("user ==> " + userId + "      service id ==> " + serviceId);
		List<ServicePromotion> promotions = null;
		Date earliestDate = null;
		if (serviceId != null) {
			serv = serviceDAO.getService(serviceId);

			promotions = serviceDAO.getPromotion(serviceId);
			for (int i = 0; i < promotions.size(); i++) {
				ServicePromotion servicePromotion = promotions.get(i);
				Appointment appt = new Appointment(
						servicePromotion.getPromotion(),
						servicePromotion.getEffectiveDate(),
						servicePromotion.getEffectiveDate(), true);
				appt.setStyleClass("promotion");
				eventModel.addEvent(appt);
				if (earliestDate == null)
					earliestDate = servicePromotion.getEffectiveDate();

			}
		}

		long days = 10;

		if (earliestDate != null) {
			days = (earliestDate.getTime() - c.getTimeInMillis())
					/ (1000 * 60 * 60 * 24);
		}
		if (user != null) {
			List<String> weathers = WeatherForcast.getWeekForcast(user.getCity());
			if (weathers != null && weathers.size()>1){
				currentWeather = weathers.get(0);
				for (int i = 1; i < weathers.size(); i++) {
					if (i >= days) break;
					
					Appointment appt = new Appointment(weathers.get(i),
							c.getTime(), c.getTime(), true);
					appt.setStyleClass("weather");
				
					eventModel.addEvent(appt);
		
					days++;
					c.add(Calendar.DATE, 1);
					

				}
			}
		}

		List<Holiday> holidays = holidayDAO.getHolidays("canada");
		log.debug("user ==> " + userId + "    holidays size ==> "
				+ holidays.size());
		if (null != holidays) {
			for (Holiday holiday : holidays) {
				c.setTime(holiday.getDate());
				c.set(Calendar.HOUR, 2);

				c2.setTime(holiday.getDate());

				c2.set(Calendar.HOUR, 23);

				Appointment appt = new Appointment(holiday.getHolidayName(),
						c.getTime(), c2.getTime(), false);
				appt.setStyleClass("calholiday");

				eventModel.addEvent(appt);
			}
		}
		if (serv != null) {
			// Non working days
			Integer[] noServiceDay = serv.getNoServiceDay();
			for (int i = 0; i < noServiceDay.length; i++) {

				c.setTime(new Date());
				c.set(Calendar.DAY_OF_WEEK, noServiceDay[i]);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);

				Date startTime = c.getTime();

				for (int weekNo = 0; weekNo <= 26; weekNo++) {

					c.set(Calendar.HOUR_OF_DAY, 2);
					startTime = c.getTime();

					c.set(Calendar.HOUR_OF_DAY, 23);
					// System.out.println(startTime+"------"+c.getTime());
					Appointment appt = new Appointment("No Working Day",
							startTime, c.getTime(), false);
					appt.setStyleClass("noworking");
					eventModel.addEvent(appt);
					c.add(Calendar.DAY_OF_YEAR, 7);
					c.set(Calendar.HOUR_OF_DAY, 0);
				}
			}
		}
	}

	public void changeServiceTime() {

		if (null != newAppointment) {
			c.setTime(newAppointment.getStartDate());
			c.add(Calendar.MINUTE, newAppointment.getServiceTime());
			newAppointment.setEndDate(c.getTime());
		}

	}

	public void changeServiceTimeExist() {

		if (null != existingAppointment.getStartDate()) {
			c.setTime(existingAppointment.getStartDate());
			c.add(Calendar.MINUTE, existingAppointment.getServiceTime());
			existingAppointment.setEndDate(c.getTime());
		}

	}

	public void increment() {
		if (null != newAppointment.getEndDate()) {
			Calendar c = Calendar.getInstance();
			c.setTime(newAppointment.getEndDate());
			c.add(Calendar.MINUTE, serv.getIncrementTime());
			if ((c.getTime().getTime() - newAppointment.getStartDate()
					.getTime()) / (1000 * 60) <= serv.getMaxTime()) {
				newAppointment.setEndDate(c.getTime());
			}
		} else if (null != existingAppointment.getEndDate()) {
			Calendar c = Calendar.getInstance();
			c.setTime(existingAppointment.getEndDate());
			c.add(Calendar.MINUTE, serv.getIncrementTime());
			if ((c.getTime().getTime() - existingAppointment.getStartDate()
					.getTime()) / (1000 * 60) <= serv.getMaxTime()) {
				existingAppointment.setEndDate(c.getTime());
			}
		}
	}

	public void decrement() {
		if (null != newAppointment.getEndDate()) {
			Calendar c = Calendar.getInstance();
			c.setTime(newAppointment.getEndDate());
			c.add(Calendar.MINUTE, -serv.getIncrementTime());
			if ((c.getTime().getTime() - newAppointment.getStartDate()
					.getTime()) / (1000 * 60) >= serv.getMinTime()) {
				newAppointment.setEndDate(c.getTime());
			}
		} else if (null != existingAppointment.getEndDate()) {
			Calendar c = Calendar.getInstance();
			c.setTime(existingAppointment.getEndDate());
			c.add(Calendar.MINUTE, -serv.getIncrementTime());
			if ((c.getTime().getTime() - existingAppointment.getStartDate()
					.getTime()) / (1000 * 60) >= serv.getMinTime()) {
				existingAppointment.setEndDate(c.getTime());
			}
		}
	}

	public void updateFrom() {

		if (null != newAppointment.getEndDate()) {
			c.setTime(newAppointment.getEndDate());
			c.add(Calendar.MINUTE, serv.getIncrementTime());

			String timeStr = dateFormat.format(c.getTime());
			if (timeStr.compareTo(serv.getEndTime()) > 0) {
				return;
			}

			newAppointment.setEndDate(c.getTime());

			c.setTime(newAppointment.getStartDate());
			c.add(Calendar.MINUTE, serv.getIncrementTime());
			newAppointment.setStartDate(c.getTime());
		} else {
			updateFromExist();
		}
	}

	public void updateFromExist() {

		if (null != existingAppointment.getEndDate()) {
			c.setTime(existingAppointment.getEndDate());
			c.add(Calendar.MINUTE, serv.getIncrementTime());

			String timeStr = dateFormat.format(c.getTime());
			if (timeStr.compareTo(serv.getEndTime()) > 0) {
				return;
			}

			existingAppointment.setEndDate(c.getTime());

			c.setTime(existingAppointment.getStartDate());
			c.add(Calendar.MINUTE, serv.getIncrementTime());
			existingAppointment.setStartDate(c.getTime());
		}

	}

	public void addEvent(ActionEvent actionEvent) {

		if (null == newAppointment
				|| newAppointment.getStartDate().equals(
						newAppointment.getEndDate())
				|| ((newAppointment.getStartDate().getTime() - new Date()
						.getTime()) / (1000 * 60 * 60) < 2)) {
			WebUtil.addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
					WebUtil.getMessage("message.notimespan.summary"), WebUtil
							.getMessage("message.notimespan.detail")));

			log.debug("no time span.");

			return;
		}

		if (newAppointment.getStartDate().getHours() == 0) {

			return;
		}

		final ServiceBooking booking = createServiceBooking();

		if (null == booking) {
			log.error("Failed to create ServiceBooking");
			return;
		}

		try {
			User user = userDAO.getUser(userId);
			booking.setUserName(user.getFirstname() + " " + user.getLastname());
			booking.setPhone(user.getPhone());
			booking.setEmail(user.getEmail());
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		booking.setUserId(userId);
		booking.setProviderId(serv.getProvider());
		booking.setProvider(serv.getProvider());
		booking.setServiceName(serv.getServiceName());
		booking.setStartTime(newAppointment.getStartDate());
		booking.setEndTime(newAppointment.getEndDate());
		booking.setNotes(newAppointment.getNotes());
		booking.setStatus(Status.REQUESTED);
		if (getServiceBookingDAO().isNotAvailable(booking)) {
			WebUtil.addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
					WebUtil.getMessage("message.booking.notavailable"), WebUtil
							.getMessage("message.booking.notavailable.detail")));
			return;
		}

		if (newAppointment.getId() == null) {
			eventModel.addEvent(newAppointment);

			getServiceBookingDAO().addServiceBooking(booking);
			newAppointment.setTitle(getEventTitle(booking));
			// newAppointment.setTitle(userId);
			newAppointment.setServiceBooking(booking);

			newAppointment.setStyleClass("ownEvent");

			Runnable r = new Runnable() {
				@Override
				public void run() {
					EmailUtil.notifyProvider(serv, booking);
				}
			};
			new Thread(r).start();

		}

	}

	public void changeEvent(ActionEvent actionEvent) {
		if (null != existingAppointment && null != existingAppointment.getId()) {
			if ((existingAppointment.getStartDate().getTime() - new Date()
					.getTime()) / (1000 * 60 * 60) < 3) {
				WebUtil.addMessage(new FacesMessage(
						FacesMessage.SEVERITY_INFO,
						WebUtil.getMessage("message.booking.notavailable"),
						WebUtil.getMessage("message.booking.notavailable.detail")));
				return;
			}

			existingAppointment.getServiceBooking().setStartTime(
					existingAppointment.getStartDate());
			existingAppointment.getServiceBooking().setEndTime(
					existingAppointment.getEndDate());
			existingAppointment.getServiceBooking().setStatus(Status.UPDATED);

			try {
				getServiceBookingDAO().updateServiceBooking(
						existingAppointment.getServiceBooking());
				eventModel.updateEvent(existingAppointment);
				Runnable r = new Runnable() {
					@Override
					public void run() {
						EmailUtil.notifyProvider(serv,
								existingAppointment.getServiceBooking());
					}
				};
				new Thread(r).start();

			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public void cancelEvent(ActionEvent actionEvent) {
		if (null != existingAppointment && null != existingAppointment.getId()) {
			if ((existingAppointment.getServiceBooking().getStartTime()
					.getTime() - new Date().getTime())
					/ (1000 * 60 * 60) < 3) {
				WebUtil.addMessage(new FacesMessage(
						FacesMessage.SEVERITY_INFO,
						WebUtil.getMessage("message.booking.notavailable"),
						WebUtil.getMessage("message.booking.notavailable.detail")));
				return;
			}

			eventModel.deleteEvent(existingAppointment);

			try {
				getServiceBookingDAO().deleteServiceBooking(
						existingAppointment.getServiceBooking()
								.getServiceBookingId());
				Runnable r = new Runnable() {
					@Override
					public void run() {
						EmailUtil.notifyProviderCancell(existingAppointment
								.getServiceBooking());
					}
				};
				new Thread(r).start();

			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public void onDateSelect(SelectEvent selectEvent) {
		if (userId == null) {
			try {
				FacesContext
						.getCurrentInstance()
						.getExternalContext()
						.redirect(
								FacesContext.getCurrentInstance()
										.getExternalContext()
										.getRequestContextPath()
										+ "/login.jsf");
			} catch (Exception e) {
				log.error(e.getCause(), e);

			}
		}

		Date start = (Date) selectEvent.getObject();
		Date end = (Date) selectEvent.getObject();
		if ((start.getTime() - new Date().getTime()) / (1000 * 60 * 60) < 4) {
			return;
		}
		if (start.getHours() <= 1) {
			return;
			/*
			 * int serviceTime=WebUtil.minuteBetween(serv.getStartTime(),
			 * serv.getEndTime()); if(userId.equals(serv.getProvider()) ||
			 * serv.getMaxTime()>=serviceTime){
			 * 
			 * start.setHours(WebUtil.extractHour(serv.getStartTime()));
			 * start.setMinutes(WebUtil.extractMinute(serv.getStartTime()));
			 * c.setTime(start); c.add(Calendar.MINUTE, serviceTime);
			 * newAppointment = new Appointment(null, "", start, c.getTime());
			 * }else{ start.setHours(WebUtil.extractHour(serv.getStartTime()));
			 * start.setMinutes(WebUtil.extractMinute(serv.getStartTime()));
			 * c.setTime(start); c.add(Calendar.MINUTE, serv.getMaxTime());
			 * newAppointment = new Appointment(null, "",start, c.getTime()); }
			 */
		} else {
			c.setTime(start);
			/*
			 * c.add(Calendar.HOUR, -1); start=c.getTime();
			 */
			int serviceTime = serv.getMinTime();
			c.add(Calendar.MINUTE, serviceTime);
			end = c.getTime();
			newAppointment = new Appointment(null, "", start, end);
		}
		RequestContext.getCurrentInstance().execute(
				"PF('addingDialog').show();");
	}

	public ServiceDAO getServiceDAO() {
		return serviceDAO;
	}

	public void setServiceDAO(ServiceDAO serviceDAO) {
		this.serviceDAO = serviceDAO;
	}

}
