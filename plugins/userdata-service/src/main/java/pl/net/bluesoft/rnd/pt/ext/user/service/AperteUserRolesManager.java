package pl.net.bluesoft.rnd.pt.ext.user.service;

import java.util.Collection;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.roles.exception.RoleCreationExceptionException;
import pl.net.bluesoft.rnd.processtool.roles.exception.RoleNotFoundException;
import pl.net.bluesoft.rnd.processtool.roles.exception.UserWithRoleNotFoundException;
import pl.net.bluesoft.rnd.pt.ext.user.dao.UserRoleDAO;
import pl.net.bluesoft.rnd.pt.ext.user.dao.UserRoleDAOImpl;
import pl.net.bluesoft.rnd.pt.ext.user.model.PersistentUserRole;

public class AperteUserRolesManager implements IUserRolesManager 
{
	@Autowired
	private ProcessToolRegistry processToolRegistry;

	public AperteUserRolesManager()
	{
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	@Override
	public boolean isRoleExist(String roleName) 
	{
		return false;
	}

	@Override
	public void createRole(String roleName, String description) throws RoleCreationExceptionException 
	{
		UserRoleDAO userRoleDao = new UserRoleDAOImpl(getSession());
		
		PersistentUserRole userRole = new PersistentUserRole(roleName, description);

		userRoleDao.saveOrUpdate(userRole);

	}

	@Override
	public void updateRoleDescription(String roleName, String description) throws RoleNotFoundException 
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<String> getRoleNamesForCompanyId(Long companyId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<UserData> getUsersByRole(String roleName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getAllRolesNames() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	private Session getSession()
	{
		Session session = processToolRegistry.getSessionFactory().getCurrentSession();
		if(session == null)
			session = processToolRegistry.getSessionFactory().openSession();
		
		return session;
	}

	@Override
	public UserData getFirstUserWithRole(String roleName)
			throws RoleNotFoundException, UserWithRoleNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

}