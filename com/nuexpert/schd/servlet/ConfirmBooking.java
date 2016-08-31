package com.nuexpert.schd.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nuexpert.schd.db.ServiceBookingDAO;
import com.nuexpert.schd.util.EmailUtil;
import com.nuexpert.schd.util.Status;
import com.nuexpert.schd.vo.ServiceBooking;


public class ConfirmBooking extends HttpServlet {
	
	private ServiceBookingDAO serviceBookingDAO;	
	
public ServiceBookingDAO getServiceBookingDAO() {
		return serviceBookingDAO;
	}

	public void setServiceBookingDAO(ServiceBookingDAO serviceBookingDAO) {
		this.serviceBookingDAO = serviceBookingDAO;
	}


	public void init() throws ServletException{ 
		setServiceBookingDAO(new ServiceBookingDAO());
		System.out.println("confirm booking inited");

	}
	
	 

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		
		String bookingId = request.getParameter("bookingid");

		try{
			final ServiceBooking booking =getServiceBookingDAO().getServiceBooking(bookingId);
			booking.setStatus(Status.CONFIRMED);
			getServiceBookingDAO().updateStatus(Status.CONFIRMED,Integer.parseInt(bookingId));
	
			 Runnable r = new Runnable()
             {
                @Override
                public void run()
                {
                	System.out.println("Email to:"+booking);
                	EmailUtil.notifyUser(booking);
                }
             };
             new Thread(r).start();
             PrintWriter out = response.getWriter();
     		out.println("<html>");
     		out.println("<body>");
     		out.println("<h1>This booking has been confirmed</h1>");
     		out.println("</body>");
     		out.println("</html>");	
	
		}catch(Exception e){
			e.printStackTrace();
            PrintWriter out = response.getWriter();
    		out.println("<html>");
    		out.println("<body>");
    		out.println("<h1>This booking has not been confirmed, please login to confirm it</h1>");
    		out.println("</body>");
    		out.println("</html>");	
			
		}
		
		
		//super.service(arg0, arg1);
	}

}
