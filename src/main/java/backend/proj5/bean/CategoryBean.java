package backend.proj5.bean;

import backend.proj5.dao.CategoryDao;
import backend.proj5.dto.Category;
import backend.proj5.entity.CategoryEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;

@Stateless
public class CategoryBean implements Serializable {

    private static final Logger logger = LogManager.getLogger(TaskBean.class);

    @EJB
    CategoryDao categoryDao;

    public boolean newCategory(String name) {
        logger.info("Creating new category: {}", name);
        boolean created = false;

        if (name != null) {
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setName(name);
            categoryDao.persist(categoryEntity);
            created = true;

            logger.info("Category created: {}", name);
        } else {
            logger.error("Category not created: {}", name);
        }

        return created;
    }

    public boolean categoryExists(String name){
        logger.info("Checking if category exists: {}", name);
        boolean exists = false;

        if (name != null) {
            CategoryEntity categoryEntity = categoryDao.findCategoryByName(name);
            if (categoryEntity != null) {
                exists = true;
                logger.info("Category exists: {}", name);
            } else {
                logger.error("Category does not exist: {}", name);
            }
        }

        return exists;
    }

    public ArrayList<Category> findAllCategories(){
        logger.info("Finding all categories");
        ArrayList<Category> categories = new ArrayList<>();
        ArrayList<CategoryEntity> categoryEntities = categoryDao.findAllCategories();

        for (CategoryEntity categoryEntity : categoryEntities) {
            categories.add(convertCategoryEntityToCategoryDto(categoryEntity));
        }

        logger.info("Found all categories");

        return categories;
    }

    public boolean deleteCategory(String name){
        logger.info("Deleting category: {}", name);
        boolean deleted = false;

        if (name != null) {
            deleted = categoryDao.deleteCategory(name);
            if (deleted) {
                logger.info("Category deleted: {}", name);
            } else {
                logger.error("Category not deleted: {}", name);
            }
        }

        return deleted;
    }

    public boolean editCategory(String name, String newName){
        logger.info("Editing category: {}", name);
        boolean edited = false;

        if (name != null && newName != null) {
            edited = categoryDao.editCategory(name, newName);
            if (edited) {
                logger.info("Category edited: {}", name);
            } else {
                logger.error("Category not edited: {}", name);
            }
        }

        return edited;
    }

    public CategoryEntity convertCategoryToEntity(String name){
        logger.info("Converting category to entity: {}", name);
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setName(name);
        return categoryEntity;
    }

    public Category convertCategoryEntityToCategoryDto(CategoryEntity categoryEntity){
        logger.info("Converting category entity to dto");
        Category category = new Category();
        category.setName(categoryEntity.getName());
        return category;
    }

    public ArrayList<String> listCategoriesByNumberOfTasks(){
        logger.info("Listing categories by number of tasks");
        ArrayList<String> categories = new ArrayList<>();
        ArrayList<CategoryEntity> categoryEntities = categoryDao.listCategoriesByNumberOfTasks();
        for (CategoryEntity categoryEntity : categoryEntities) {
            categories.add(categoryEntity.getName());
        }

        logger.info("Listed categories by number of tasks");

        return categories;
    }

}

