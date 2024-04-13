package backend.proj5.dao;

import backend.proj5.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.util.ArrayList;
import java.util.List;

@Stateless
public class UserDao extends AbstractDao<UserEntity> {

	private static final long serialVersionUID = 1L;

	public UserDao() {
		super(UserEntity.class);
	}


	public UserEntity findUserByToken(String token) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByToken").setParameter("token", token)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public UserEntity findUserByUsername(String username) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByUsername").setParameter("username", username)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}


	public UserEntity findUserByEmail(String email) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByEmail").setParameter("email", email)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public UserEntity findUserByPhone(String phone) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByPhone").setParameter("phone", phone)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public UserEntity findUserByUsernameAndPassword(String username, String password){
		try{
			return (UserEntity) em.createNamedQuery("User.findUserByUsernameAndPassword")
					.setParameter("username", username)
					.setParameter("password", password)
					.getSingleResult();
		}catch (NoResultException e){
			return null; //Nenhum user foi encontrado com estes dados
		}
	}

	public ArrayList<UserEntity> findAllUsers() {
		try {
			return (ArrayList<UserEntity>) em.createNamedQuery("User.findAllUsers").getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	public ArrayList<UserEntity> findAllUsersByTypeOfUser(int typeOfUser) {
		try {
			return (ArrayList<UserEntity>) em.createNamedQuery("User.findAllUsersByTypeOfUser").setParameter("typeOfUser", typeOfUser).getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	public ArrayList<UserEntity> findAllUsersByVisibility(boolean visible) {
		try {
			return (ArrayList<UserEntity>) em.createNamedQuery("User.findAllUsersByVisibility").setParameter("visible", visible).getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	public ArrayList<UserEntity> findAllUsersByTypeOfUserAndVisibility(int typeOfUser, boolean visible) {
		try {
			return (ArrayList<UserEntity>) em.createNamedQuery("User.findAllUsersByTypeOfUserByVisibility").setParameter("typeOfUser", typeOfUser)
					.setParameter("visible", visible).getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	public boolean doesUserHavePasswordDefined(String validationToken) {
		try {
			return (boolean) em.createNamedQuery("User.doesUserHavePasswordDefined").setParameter("validationToken", validationToken).getSingleResult();
		} catch (Exception e) {
			return false;
		}
	}

	public UserEntity findUserByValidationToken(String validationToken) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByValidationToken").setParameter("validationToken", validationToken)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public int countAllUsers() {
		try {
			return ((Long) em.createNamedQuery("User.countAllUsers").getSingleResult()).intValue();
		} catch (Exception e) {
			return 0;
		}
	}

	public int countAllUsersByConfirmed(boolean confirmed) {
		try {
			return ((Long) em.createNamedQuery("User.countAllUsersByConfirmed").setParameter("confirmed", confirmed).getSingleResult()).intValue();
		} catch (Exception e) {
			return 0;
		}
	}

	public int countAllUsersByVisibility(boolean visible) {
		try {
			return ((Long) em.createNamedQuery("User.countAllUsersByVisibility").setParameter("visible", visible).getSingleResult()).intValue();
		} catch (Exception e) {
			return 0;
		}
	}

}
