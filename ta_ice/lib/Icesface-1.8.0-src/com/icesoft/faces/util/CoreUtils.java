package com.icesoft.faces.util;

import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import org.w3c.dom.Element;

import com.icesoft.faces.application.D2DViewHandler;
import com.icesoft.faces.context.DOMContext;
import com.icesoft.faces.renderkit.dom_html_basic.DomBasicRenderer;

public class CoreUtils {
	private static Boolean renderPortletStyleClass;
	private static Boolean portletEnvironment;

    public static String resolveResourceURL(FacesContext facesContext, String path) {
        return facesContext.getApplication().getViewHandler().getResourceURL(facesContext, path);
    }
    
    public static boolean isPortletEnvironment() {
    	if (portletEnvironment == null) {
		    try {
		    	portletEnvironment = new Boolean(FacesContext.getCurrentInstance().getExternalContext()
		    			.getRequest() instanceof javax.portlet.PortletRequest);
		    } catch (java.lang.NoClassDefFoundError e) {
		    	//portlet not found
		    	portletEnvironment = Boolean.FALSE;
			}
    	}
    	return portletEnvironment.booleanValue();
    }
    
    public static String getPortletStyleClass(String className) {
    	if (isPortletEnvironment() && isRenderPortletStyleClass()) {
    		return " "+ className;
    	}
    	return "";
    }
    
    private static boolean isRenderPortletStyleClass() {
    	if (renderPortletStyleClass == null) {
    		String renderStyle = FacesContext.getCurrentInstance().getExternalContext().
			 getInitParameter("com.icesoft.faces.portlet.renderStyles");
    		if (renderStyle == null) {
    			//default is true
    			renderPortletStyleClass = Boolean.TRUE;
    		} else {
    			renderPortletStyleClass = Boolean.valueOf(renderStyle);
    		}
    		
    	}
    	return renderPortletStyleClass.booleanValue();
    }
    
    public static String addPortletStyleClassToQualifiedClass(String qualifiedStyleClass, 
    																String defaultClass, 
    																String portletClass) {
    	return addPortletStyleClassToQualifiedClass(qualifiedStyleClass, 
    												defaultClass, 
    												portletClass, 
    												false);
    		 
    }

    public static String addPortletStyleClassToQualifiedClass(String qualifiedStyleClass, 
    															String defaultClass, 
    															String portletClass, 
    															boolean disabled) {
    	if (isPortletEnvironment() && isRenderPortletStyleClass()) {
	    	if (disabled) {
	    		return qualifiedStyleClass.replaceAll(defaultClass+"-dis", 
	    				defaultClass + "-dis" + " " + portletClass);
	    	} else {
	    		return qualifiedStyleClass.replaceAll(defaultClass, 
	    				defaultClass + " " + portletClass);
	    	}
    	}else {
    		return qualifiedStyleClass;
    	}
    }

    /*
     * This method will help to retain the faces messages on the partialSubmit, 
     * the page refresh and the dynamic component rendering.
     * It will be called by two classes the DomBasicRenderer and the MessageRenderer.
     * 
     * Calling it from the DomBasicRenderer insures that there will always be a 
     * message in the default messages Map, only if the component was invalid. 
     * So if the jsp document has an "ice" or "h" messages component they can 
     * serve the default messages map. 
     * 
     * Calling this method from the MessageRenderer gives the component ordering
     * flexibility. (e.g) It doesn't matter if the "INPUT" component was rendered 
     * first or the "message" component.
     * 
     * Calling this method from two renderer will not cause any problem or side effect.
     * The first calling class will complete the work, and the next one will already 
     * find a message and won't repeat the process.
     */
    public static void recoverFacesMessages(FacesContext facesContext, UIComponent uiComponent) {
        if (!(uiComponent instanceof UIInput)) return;
        UIInput input = (UIInput) uiComponent;
        String clientId = input.getClientId(facesContext);
        String localFacesMsgId = clientId + "$ice-msg$";
        String localRequired = clientId + "$ice-req$";  
        //save the required attribute, specifically for UIData
        if (input.getAttributes().get(localRequired) == null) {
            // this property will be used by the UISeries.restoreRequiredAttribute()
            input.getAttributes().put(localRequired,new Boolean(input.isRequired()));
        }
        //component is invalid there should be a message in the default messages map
        if (!input.isValid()) {
            Iterator messages = facesContext.getMessages(clientId);
            FacesMessage message = null;
            //if no message found, then it might a page refresh call
            //or the request of dynamic rendering of the component. 
            //if so, get the message from the component's map and add
            //it to the default messages map
            if (messages == null || !messages.hasNext()) {
                if(input.getAttributes().get(localFacesMsgId) != null) {
                    message = (FacesMessage) input.getAttributes().get(localFacesMsgId);
                    facesContext.addMessage(clientId, message);
                }
            } else {//if found, then store it to the component's message map,
                //so can be served later.
                message = (FacesMessage) messages.next();
                input.getAttributes().put(localFacesMsgId,message );
            }
        } else { //component is valid, so remove the old message.
            input.getAttributes().remove(localFacesMsgId);
        }        
    }
    public static int toolTipcounter = 1;
    public static void addPanelTooltip(FacesContext facesContext, UIComponent uiComponent) {
        DOMContext domContext = DOMContext.getDOMContext(facesContext, uiComponent);
        if (uiComponent.getAttributes().get("panelTooltip") == null) return;
        String panelTooltipId = String.valueOf(uiComponent.getAttributes().get("panelTooltip"));
        int delay = 500 ;
        String hideOn = "mouseout";
        boolean dynamic = false;
        String formId = "";
        String ctxValue = "";

            UIComponent panelTooltip = D2DViewHandler.findComponent(panelTooltipId, uiComponent);
            if (panelTooltip != null/* && family type equals panelPopup*/) { 
                //replace the id with the clientid
                panelTooltipId = panelTooltip.getClientId(facesContext);
                if (panelTooltip.getAttributes().get("hideOn") != null) {
                    hideOn = String.valueOf(panelTooltip.getAttributes().get("hideOn"));
                }
                if (panelTooltip.getAttributes().get("dynamic") != null) {
                    dynamic = ((Boolean)panelTooltip.getAttributes().get("dynamic")).booleanValue();
                }                
                if (panelTooltip.getAttributes().get("hoverDelay") != null) {
                    delay = new Integer(String.valueOf(panelTooltip.getAttributes()
                            .get("hoverDelay"))).intValue();
                }
                if (uiComponent.getAttributes().get("contextValue") != null) {
                    ctxValue = String.valueOf(uiComponent.getAttributes().get("contextValue"));
                }
            }
            UIComponent form = DomBasicRenderer.findForm(panelTooltip);
            if (form != null) {
                formId = form.getClientId(facesContext);
            }

        Element rootElement = (Element) domContext.getRootNode();
        String onmouseover = String.valueOf(rootElement.getAttribute("onmouseover"));
        onmouseover+="; new ToolTipPanelPopup(this, '"+ panelTooltipId +"', event, '"+ 
        hideOn +"','"+ delay+"', '"+ dynamic+"', '"+ formId +"', '"+ ctxValue +"','"+
                CoreUtils.resolveResourceURL(facesContext, "/xmlhttp/blank")+"');";
        rootElement.setAttribute("onmouseover", onmouseover);
    }
   
    public static boolean objectsEqual(Object ob1, Object ob2) {
        if (ob1 == null && ob2 == null) {
            return true;
        }
        if (ob1 == null || ob2 == null) {
            return false;
        }
        return ob1.equals(ob2);
    }

    public static boolean throwablesEqual(Throwable th1, Throwable th2) {
        if (th1 == null && th2 == null) return true;
        if (th1 == null || th2 == null) return false;
        if (th1.getClass() != th2.getClass()) return false;

        if (!th1.getMessage().equals(th2.getMessage())) return false;

        StackTraceElement[] st1 = th1.getStackTrace();
        StackTraceElement[] st2 = th2.getStackTrace();
        if (st1.length != st2.length) return false;

        for (int i = 0; i < st1.length; i++) {
            if (!st1[i].equals(st2[i])) return false;
        }
        return true;
    }
}
