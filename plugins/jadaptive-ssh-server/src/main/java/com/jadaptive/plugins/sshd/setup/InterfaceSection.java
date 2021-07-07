package com.jadaptive.plugins.sshd.setup;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.setup.SetupSection;
import com.jadaptive.api.wizards.WizardState;
import com.jadaptive.plugins.sshd.SSHInterface;
import com.jadaptive.plugins.sshd.SSHInterfaceService;
import com.jadaptive.utils.ObjectUtils;


public abstract class InterfaceSection extends SetupSection {

	private static final String INTERFACE_UUID = "interfaceUUID";
	
	@Autowired
	protected SSHInterfaceService interfaceService; 
	
	public InterfaceSection() {
		super(CreateInterface.RESOURCE_KEY, 
				"configureInterface", 
				"/com/jadaptive/plugins/sshd/setup/CreateInterface.html", 
				SetupSection.START_OF_DEFAULT + 3);
	}
	
	@Override
	public void processReview(Document document, WizardState state, Integer sectionIndex) {

		Element content = document.selectFirst("#setupStep");
		CreateInterface iface = ObjectUtils.assertObject(state.getObjectAt(sectionIndex), CreateInterface.class);
		
		content	.appendChild(new Element("div")
						.addClass("col-12 w-100 my-3")
						.appendChild(new Element("h4")
							.attr("jad:i18n", "review.interface.header")
							.attr("jad:bundle", getBundle()))
					.appendChild(new Element("p")
							.attr("jad:bundle", getBundle())
							.attr("jad:i18n", "review.interface.desc"))
					.appendChild(new Element("div")
							.addClass("row")
							.appendChild(new Element("div")
									.addClass("col-3")
									.appendChild(new Element("span")
											.attr("jad:bundle", "createInterface")
											.attr("jad:i18n", "addressToBind.name")))
							.appendChild(new Element("div")
										.addClass("col-9")
										.appendChild(new Element("span")
												.appendChild(new Element("strong")
														.text(iface.getAddressToBind()))))
							.appendChild(new Element("div")
									.addClass("col-3")
									.appendChild(new Element("span")
													.attr("jad:bundle", "createInterface")
													.attr("jad:i18n", "port.name")))
							.appendChild(new Element("div")
									.addClass("col-9")
									.appendChild(new Element("span")
											.appendChild(new Element("strong")
											.text(String.valueOf(iface.getPort())))))));
	}
	
	@Override
	public final void finish(WizardState state, Integer sectionIndex) {
		
		CreateInterface iface = ObjectUtils.assertObject(state.getObjectAt(sectionIndex), CreateInterface.class);
		String uuid = (String) state.getParameter(INTERFACE_UUID);
		if(StringUtils.isNotBlank(uuid)) {
			interfaceService.deleteObject(interfaceService.getObjectByUUID(uuid));
		}
		SSHInterface sshIface = createInterface(iface);
		interfaceService.saveOrUpdate(sshIface);
		state.setParameter(INTERFACE_UUID, sshIface.getUuid());
		
	}

	protected abstract SSHInterface createInterface(CreateInterface iface);
		
}