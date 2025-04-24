package com.danieljoaco.storeapp.menu.adminMenu.tables;

import com.danieljoaco.storeapp.users.Users;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

import java.util.List;

import static com.danieljoaco.storeapp.users.UserDao.getAllUsers;

/**
 * Controller for the users table
 */
public class UsersTableController extends BaseTableController<Users> {

    @Override
    protected void setupColumns() {
        TableColumn<Users, String> colUserId = createColumn("Id", "id", 80);
        TableColumn<Users, String> colUserName = createColumn("Name", "name", 160);
        TableColumn<Users, String> colUserEmail = createColumn("Email", "email", 200);
        TableColumn<Users, String> colUserType = createColumn("User type", "typeUser", 100);
        configureCapitalizeColumn(colUserType);
        TableColumn<Users, String> colUserCreateAt = createColumn("Create at", "formattedDate", 80);

        tableView.getColumns().addAll(
                colUserId, colUserName, colUserEmail, colUserType, colUserCreateAt
        );
    }

    @Override
    public void loadData() {
        dataList.clear();
        List<Users> users = getAllUsers();
        dataList.addAll(users);

        if (dataList.isEmpty()) {
            System.out.println("There are no users to show.");
        } else {
            System.out.println("Loaded " + dataList.size() + " users.");
        }
    }

    /**
     * Configures a column to capitalize the displayed text
     * @param column The column to configure
     */
    private void configureCapitalizeColumn(TableColumn<Users, String> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(capitalize(item));
                }
            }
        });
    }
}