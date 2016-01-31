/**
 * 
 */
package com.softpoint.optima.control;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContext;

import com.softpoint.optima.JsonRpcInitializer;

/**
 * @author WDARWISH
 *
 */
public class EntityController<E> {
	
	protected ServletContext servletContext;
	protected boolean autoClose;
	protected EntityManager localManager;
	
	/**
	 * @return the autoClose
	 */
	public boolean isAutoClose() {
		return autoClose;
	}

	/**
	 * @param autoClose the autoClose to set
	 */
	public void setAutoClose(boolean autoClose) {
		this.autoClose = autoClose;
	}

	/**
	 * @return the servletContext
	 */
	public ServletContext getServletContext() {
		return servletContext;
	}

	/**
	 * @param servletContext the servletContext to set
	 */
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	/**
	 * 
	 */
	public EntityController(ServletContext servletContext) {
		this.servletContext = servletContext;
		autoClose = true;
	}
	
	public EntityController(ServletContext servletContext , boolean autoClose) {
		this.servletContext = servletContext;
		this.autoClose = autoClose;
		if (autoClose == false) {
			EntityManagerFactory factory = (EntityManagerFactory)servletContext.getAttribute(JsonRpcInitializer.__ENTITY_FACTORY);
			localManager = factory.createEntityManager();
		}
	}
	
	/**
	 * @param e
	 * @throws EntityControllerException
	 */
	public void persist(E e) throws EntityControllerException {
		try {
			EntityManagerFactory factory = (EntityManagerFactory)servletContext.getAttribute(JsonRpcInitializer.__ENTITY_FACTORY);
			EntityManager manager = autoClose?factory.createEntityManager():localManager;
			manager.getTransaction().begin();
			manager.persist(e);
			manager.getTransaction().commit();
			if (autoClose) {
				manager.close();
			}
	   } catch (Throwable t) {
		   t.printStackTrace();
			throw new EntityControllerException("DB001 - persist - " + t.getMessage() , t);
		}
	}
	
	public void detach(E e) throws EntityControllerException {
		try {
			EntityManagerFactory factory = (EntityManagerFactory)servletContext.getAttribute(JsonRpcInitializer.__ENTITY_FACTORY);
			EntityManager manager = autoClose?factory.createEntityManager():localManager;
			manager.detach(e);
			if (autoClose) {
				manager.close();
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw new EntityControllerException("DB002 - remove - " + t.getMessage() , t);
		}
	}
	/**
	 * @param e
	 * @throws EntityControllerException
	 */
	public void remove(E e) throws EntityControllerException {
		try {
			EntityManagerFactory factory = (EntityManagerFactory)servletContext.getAttribute(JsonRpcInitializer.__ENTITY_FACTORY);
			EntityManager manager = autoClose?factory.createEntityManager():localManager;
			manager.getTransaction().begin();
			manager.remove(e);
			manager.getTransaction().commit();
			if (autoClose) {
				manager.close();
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw new EntityControllerException("DB002 - remove - " + t.getMessage() , t);
		}
	}
	
	/**
	 * @param e
	 * @throws EntityControllerException
	 */
	public void remove(Class<E> cls , Object key) throws EntityControllerException {
		try {
			EntityManagerFactory factory = (EntityManagerFactory)servletContext.getAttribute(JsonRpcInitializer.__ENTITY_FACTORY);
			EntityManager manager = autoClose?factory.createEntityManager():localManager;
			E e = manager.find(cls, key);
			manager.getTransaction().begin();
			manager.remove(e);
			manager.getTransaction().commit();
			if (autoClose) {
				manager.close();
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw new EntityControllerException("DB003 remove - " + t.getMessage(), t);
		}
	}
	
	
	/**
	 * @param cls
	 * @param key
	 * @return
	 * @throws EntityControllerException
	 */
	public E find(Class<E> cls, Object key) throws EntityControllerException {
		try {
			EntityManagerFactory factory = (EntityManagerFactory)servletContext.getAttribute(JsonRpcInitializer.__ENTITY_FACTORY);
			EntityManager manager = autoClose?factory.createEntityManager():localManager;
			E e = manager.find(cls, key);
			if (e != null) {
				manager.refresh(e);
			}
			if (autoClose) {
				manager.close();
			}
			return e;
		} catch (Throwable t) {
			t.printStackTrace();
			throw new EntityControllerException("DB004 find - " + t.getMessage() , t);
		}
	}
	
	/**
	 * @param e
	 * @return
	 * @throws EntityControllerException
	 */
	public E merge(E e) throws EntityControllerException {
		try { 
			EntityManagerFactory factory = (EntityManagerFactory)servletContext.getAttribute(JsonRpcInitializer.__ENTITY_FACTORY);
			EntityManager manager = autoClose?factory.createEntityManager():localManager;
			manager.getTransaction().begin();
			e = manager.merge(e);
			manager.getTransaction().commit();
			if (autoClose) {
				manager.close();
			}
			return e;
		} catch (Throwable t) {
			t.printStackTrace();
			throw new EntityControllerException("DB005 find - " + t.getMessage() , t);
		}
	}

	
	EntityManager transactionManager;
	public void mergeTransactionStart() throws EntityControllerException {
		try { 
			EntityManagerFactory factory = (EntityManagerFactory)servletContext.getAttribute(JsonRpcInitializer.__ENTITY_FACTORY);
			transactionManager = autoClose?factory.createEntityManager():localManager;
			transactionManager.getTransaction().begin();
		} catch (Throwable t) {
			t.printStackTrace();
			throw new EntityControllerException("DB005 find - " + t.getMessage() , t);
		}
	}
	
	/**
	 * @param e
	 * @return
	 * @throws EntityControllerException
	 */
	public void mergeTransactionClose() throws EntityControllerException {
		try { 
			transactionManager.getTransaction().commit();
			if (autoClose) {
				transactionManager.close();
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw new EntityControllerException("DB005 find - " + t.getMessage() , t);
		}
	}

	/**
	 * @param e
	 * @return
	 * @throws EntityControllerException
	 */
	public E mergeTransactionMerge(E e) throws EntityControllerException {
		try { 
			e = transactionManager.merge(e);
			return e;
		} catch (Throwable t) {
			t.printStackTrace();
			throw new EntityControllerException("DB005 find - " + t.getMessage() , t);
		}
	}

	
	/**
	 * @param cls
	 * @return
	 * @throws EntityControllerException
	 */
	public List<E> findAll(Class<E> cls) throws EntityControllerException {
		try {
			EntityManagerFactory factory = (EntityManagerFactory)servletContext.getAttribute(JsonRpcInitializer.__ENTITY_FACTORY);
			EntityManager manager = autoClose?factory.createEntityManager():localManager;
			TypedQuery<E> query = manager.createQuery(String.format("SELECT o FROM %s o" , cls.getName()) , cls);
			List<E> objects = query.getResultList();
			if (autoClose) {
				manager.close();
			}
			return objects;
	    } catch (Throwable t) {
	    	t.printStackTrace();
			throw new EntityControllerException("DB006 findAll - " + t.getMessage() , t);
		}
	}

	public List<E> findAllQuery(Class<E> cls,String sqlQuery) throws EntityControllerException {
		try {
			EntityManagerFactory factory = (EntityManagerFactory)servletContext.getAttribute(JsonRpcInitializer.__ENTITY_FACTORY);
			EntityManager manager = autoClose?factory.createEntityManager():localManager;
			TypedQuery<E> query = manager.createQuery(sqlQuery , cls);
			List<E> objects = query.getResultList();
			if (autoClose) {
				manager.close();
			}
			return objects;
	    } catch (Throwable t) {
	    	t.printStackTrace();
			throw new EntityControllerException("DB006 findAll - " + t.getMessage() , t);
		}
	}

	public E refresh(E e) throws EntityControllerException  {
		try {
			EntityManagerFactory factory = (EntityManagerFactory)servletContext.getAttribute(JsonRpcInitializer.__ENTITY_FACTORY);
			EntityManager manager = autoClose?factory.createEntityManager():localManager;
			manager.refresh(e);
			if (autoClose) {
				manager.close();
			}
			return e;
	 	} catch (Throwable t) {
	    	t.printStackTrace();
			throw new EntityControllerException("DB006 findAll - " + t.getMessage() , t);
		}
	}
	
	/**
	 * @param cls
	 * @return
	 * @throws EntityControllerException
	 */
	public List<E> findAll(Class<E> cls , String opql , Object ...params) throws EntityControllerException {
		try {
			EntityManagerFactory factory = (EntityManagerFactory)servletContext.getAttribute(JsonRpcInitializer.__ENTITY_FACTORY);
			EntityManager manager = autoClose?factory.createEntityManager():localManager;
			TypedQuery<E> query = manager.createQuery(opql , cls);
			for (int index = 0 ; index < params.length ; index++) {
				query.setParameter(1, params[index]);
			}
			List<E> objects = query.getResultList();
			if (autoClose) {
				manager.close();
			}
			return objects;
		} catch (Throwable t) {
			t.printStackTrace();
			throw new EntityControllerException("DB007 findAll - " + t.getMessage() , t);
		}
	}
	
	
	/**
	 * @param cls
	 * @return
	 * @throws EntityControllerException
	 */
	public E find(Class<E> cls , String opql , Object ...params) throws EntityControllerException {
		try {
			EntityManagerFactory factory = (EntityManagerFactory)servletContext.getAttribute(JsonRpcInitializer.__ENTITY_FACTORY);
			EntityManager manager = autoClose?factory.createEntityManager():localManager;
			TypedQuery<E> query = manager.createQuery(opql , cls);
			for (int index = 0 ; index < params.length ; index++) {
				query.setParameter(index + 1, params[index]);
			}
			List<E> objects = query.getResultList();
			if (autoClose) {
				manager.close();
			}
			if (objects == null || objects.isEmpty()) {
				return null;
			} else {				
				return objects.get(0);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw new EntityControllerException("DB007 findAll - " + t.getMessage() , t);
		}
	}
	
	public int dml(Class<E> cls, String opql , Object ...params) throws EntityControllerException {
		try {
			EntityManagerFactory factory = (EntityManagerFactory)servletContext.getAttribute(JsonRpcInitializer.__ENTITY_FACTORY);
			EntityManager manager = autoClose?factory.createEntityManager():localManager;
			Query query = manager.createQuery(opql , cls);
			for (int index = 0 ; index < params.length ; index++) {
				query.setParameter(index + 1, params[index]);
			}
			manager.getTransaction().begin();
			int rowCount = 	query.executeUpdate();
			manager.getTransaction().commit();
			if (autoClose) {
				manager.close();
			}
			return rowCount;
		} catch (Throwable t) {
			t.printStackTrace();
			throw new EntityControllerException("DB008 dml - " + t.getMessage() , t);
		}
		
	}
	
	
	public List<?> nativeQuery( String sql , Object ...params) throws EntityControllerException {
		try {
			EntityManagerFactory factory = (EntityManagerFactory)servletContext.getAttribute(JsonRpcInitializer.__ENTITY_FACTORY);
			EntityManager manager = autoClose?factory.createEntityManager():localManager;
			Query query = manager.createNativeQuery(sql);
			for (int index = 0 ; index < params.length ; index++) {
				query.setParameter(index + 1, params[index]);
			}
			
			List<?> results = query.getResultList();
			if (autoClose) {
				manager.close();
			}
			return results;
			
		} catch (Throwable t) {
			t.printStackTrace();
			throw new EntityControllerException("DB009 Native - " + t.getMessage() , t);
		}
		
	}
	
	public List<E> nativeQuery( Class<E> cls , String sql , Object ...params) throws EntityControllerException {
		try {
			EntityManagerFactory factory = (EntityManagerFactory)servletContext.getAttribute(JsonRpcInitializer.__ENTITY_FACTORY);
			EntityManager manager = autoClose?factory.createEntityManager():localManager;
			Query query = manager.createNativeQuery(sql , cls);
			for (int index = 0 ; index < params.length ; index++) {
				query.setParameter(index + 1, params[index]);
			}
			
			@SuppressWarnings("unchecked")
			List<E> results = (List<E>)query.getResultList();
			if (autoClose) {
				manager.close();
			}
			return results;
			
		} catch (Throwable t) {
			t.printStackTrace();
			throw new EntityControllerException("DB010 Native - " + t.getMessage() , t);
		}
		
	}
	/**
	 * 
	 */
	public void closeLocalManager() {
		if (localManager != null && localManager.isOpen()) {
			localManager.close();
		}
	}

}
