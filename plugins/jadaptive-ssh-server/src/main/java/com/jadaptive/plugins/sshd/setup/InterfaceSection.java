package com.jadaptive.plugins.sshd.setup;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.TransactionService;
import com.jadaptive.api.setup.SetupSection;
import com.jadaptive.api.ui.wizards.Wizard;
import com.jadaptive.api.ui.wizards.WizardState;
import com.jadaptive.plugins.sshd.SSHDService;
import com.jadaptive.plugins.sshd.SSHInterface;
import com.jadaptive.plugins.sshd.SSHInterfaceService;
import com.jadaptive.utils.ObjectUtils;
import com.sshtools.common.ssh.SecurityLevel;


public abstract class InterfaceSection extends SetupSection {

	@Autowired
	protected SSHInterfaceService interfaceService; 

	@Autowired
	protected TransactionService transactionService;
	
	@Autowired
	protected SSHDService sshdService; 
	
	public InterfaceSection(String bundle) {
		super(bundle, 
				"configureInterface", 
				"/com/jadaptive/plugins/sshd/setup/CreateInterface.html", 100);
	}
	
	@Override
	public boolean isSystem() {
		return true;
	}
	
	@Override
	public Class<?> getResourceClass() {
		return CreateInterface.class;
	}
	
	protected boolean isHiddenIfPortIsFree() {
		return false;
	}
	
	protected abstract int getDefaultPort();
	
	public boolean isHidden() {
		if(isHiddenIfPortIsFree()) {
			try(Socket sock = new Socket()) {
				sock.connect(InetSocketAddress.createUnresolved("localhost", getDefaultPort()), 5000);
				return false;
			} catch(IOException e) {
				CreateInterface iface = new CreateInterface();
				iface.setAddressToBind("0.0.0.0");
				iface.setPort(getDefaultPort());
				Wizard.getCurrentState().setObject(this, iface);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void processReview(Document document, WizardState state) {

		Element content = document.selectFirst("#wizardContent");
		CreateInterface iface = ObjectUtils.assertObject(state.getObject(this), CreateInterface.class);
		
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
	public final void finish(WizardState state) {
		
		CreateInterface iface = ObjectUtils.assertObject(state.getObject(this), CreateInterface.class);
		SSHInterface sshIface = createInterface(iface);
		sshIface.setSecurityLevel(SecurityLevel.STRONG);
		interfaceService.saveOrUpdate(sshIface);
		transactionService.tx().undoable(() -> {
			sshdService.removeInterface(sshIface);
		});
		
	}

	protected abstract SSHInterface createInterface(CreateInterface iface);
		
}