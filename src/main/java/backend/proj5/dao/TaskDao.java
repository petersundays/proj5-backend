package backend.proj5.dao;

import backend.proj5.entity.TaskEntity;
import backend.proj5.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.util.ArrayList;
import java.util.List;

@Stateless
public class TaskDao extends AbstractDao<TaskEntity> {

	private static final long serialVersionUID = 1L;

	public TaskDao() {
		super(TaskEntity.class);
	}
	

	public TaskEntity findTaskById(String id) {
		try {
			return (TaskEntity) em.createNamedQuery("Task.findTaskById").setParameter("id", id)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}

	}

	public ArrayList<TaskEntity> findTasksByUser(UserEntity userEntity) {
		try {
            return (ArrayList<TaskEntity>) em.createNamedQuery("Task.findTasksByUser").setParameter("owner", userEntity).getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	public ArrayList<TaskEntity> findNotErasedTasks() {
		try {
			return (ArrayList<TaskEntity>) em.createNamedQuery("Task.findNotErasedTasks").getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	public ArrayList<TaskEntity> findErasedTasks() {
		try {
            return (ArrayList<TaskEntity>) em.createNamedQuery("Task.findErasedTasks").getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	public ArrayList<TaskEntity> findAllErasedTasksFromUser(UserEntity owner) {
		try {
			return (ArrayList<TaskEntity>) em.createNamedQuery("Task.findAllErasedTasksFromUser").setParameter("owner", owner).getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	public ArrayList<TaskEntity> findAllTasks() {
		try {
            return (ArrayList<TaskEntity>) em.createNamedQuery("Task.findAllTasks").getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	public boolean eraseTask(String id) {
		boolean erased = false;
		if (id == null) {
			erased = false;
		} else {
			try {
				TaskEntity taskToErase = findTaskById(id);
				taskToErase.setErased(true);
				merge(taskToErase);
				erased = true;
			} catch (Exception e) {
				erased = false;
			}
		}
		return erased;
	}

public boolean eraseAllNotErasedTasks() {
		boolean erased = false;
		try {
			em.createNamedQuery("Task.eraseAllNotErasedTasks").executeUpdate();
			erased = true;
		} catch (Exception e) {
			erased = false;
		}
		return erased;
	}

	public boolean deleteTask(String id) {
		boolean deleted = false;
		if (id == null) {
			deleted = false;
		} else {
			try {
				em.createNamedQuery("DeleteTask").setParameter("id", id).executeUpdate();
				deleted = true;
			} catch (Exception e) {
				deleted = false;
			}
		}
		return deleted;
	}

	public boolean deleteAllTasksFromUser(UserEntity owner) {
		boolean deleted = false;
		if (owner == null) {
			deleted = false;
		} else {
			try {
				em.createNamedQuery("Task.deleteAllErasedTasksFromUser").setParameter("owner", owner).executeUpdate();
				deleted = true;
			} catch (Exception e) {
				deleted = false;
			}
		}
		return deleted;
	}

	public boolean restoreAllErasedTasks() {
		boolean restored = false;
		try {
			em.createNamedQuery("Tasks.restoreAllErasedTasks").executeUpdate();
			restored = true;
		} catch (Exception e) {
			restored = false;
		}
		return restored;
	}

	public boolean deleteAllErasedTasks() {
		boolean deleted = false;
		try {
			em.createNamedQuery("Task.deleteAllErasedTasks").executeUpdate();
			deleted = true;
		} catch (Exception e) {
			deleted = false;
		}
		return deleted;
	}

	// Está a fazer o cast para Number e depois para int, porque ao fazer diretamente para int não retornava nada,
	// por causa do tipo de retorno do método getSingleResult() que é Object e não int.
	public int numberOfTasksFromUser(UserEntity owner) {
		try {
			Number result = (Number) em.createNamedQuery("Task.numberOfTasksFromUser")
					.setParameter("owner", owner)
					.getSingleResult();
			return result.intValue();
		} catch (Exception e) {
			return 0;
		}
	}

	public int numberOfTasksFromUserByState(UserEntity owner, int stateId) {
		try {
			Number result = (Number) em.createNamedQuery("Task.numberOfTasksFromUserByState")
					.setParameter("owner", owner)
					.setParameter("stateId", stateId)
					.getSingleResult();
			return result.intValue();
		} catch (Exception e) {
			return 0;
		}
	}

	public int countNumberOfNotErasedTasks() {
		try {
			Number result = (Number) em.createNamedQuery("Task.countNumberOfNotErasedTasks").getSingleResult();
			return result.intValue();
		} catch (Exception e) {
			return 0;
		}
	}

	public int countNumberOfTasksByState(int stateId) {
		try {
			Number result = (Number) em.createNamedQuery("Task.countNumberOfTasksByState")
					.setParameter("stateId", stateId)
					.getSingleResult();
			return result.intValue();
		} catch (Exception e) {
			return 0;
		}
	}

	public ArrayList<TaskEntity> findTasksByStateId(int stateId) {
		try {
			return (ArrayList<TaskEntity>) em.createNamedQuery("Task.findTasksByStateId").setParameter("stateId", stateId).getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	public List<Object[]> totalTasksDoneByEachDay() {
		try {
			return em.createNamedQuery("Task.totalTasksDoneByEachDay").getResultList();
		} catch (Exception e) {
			return null;
		}
	}

}
