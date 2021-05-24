package org.scada_lts.permissions.migration;

import br.org.scadabr.api.exception.DAOException;
import br.org.scadabr.db.utils.TestUtils;
import br.org.scadabr.vo.permission.ViewAccess;
import br.org.scadabr.vo.permission.WatchListAccess;
import br.org.scadabr.vo.usersProfiles.UsersProfileVO;
import com.serotonin.mango.view.ShareUser;
import com.serotonin.mango.view.View;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.permission.DataPointAccess;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.scada_lts.dao.DAO;
import org.scada_lts.dao.DataPointUserDAO;
import org.scada_lts.dao.UserDAO;
import org.scada_lts.dao.UsersProfileDAO;
import org.scada_lts.mango.adapter.MangoDataPoint;
import org.scada_lts.mango.adapter.MangoDataSource;
import org.scada_lts.mango.service.*;
import org.scada_lts.permissions.service.*;
import org.scada_lts.permissions.service.util.PermissionsUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import utils.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.scada_lts.permissions.service.util.PermissionsUtils.mergeDataPointAccessesList;


@RunWith(Parameterized.class)
public class DataPointMigrationPermissionsCommandTest {

    private final static int dataPointId1 = 11;
    private final static int dataPointId2 = 22;
    private final static int dataPointId3 = 33;
    private final static int dataPointId4 = 44;
    private final static int dataPointId5 = 55;


    @Parameterized.Parameters(name= "\n\n{index}: fromProfile: {0}, fromUser: {1}, \nfromView1: {2}, fromView2: {3}, permissionView1: {4}, permissionView2: {5}, \nexpected: {6}\n\n")
    public static Object[][] primeNumbers() {
        return new Object[][] {

                new Object[] {
                    Stream.of(new DataPointAccess(dataPointId1, 1), new DataPointAccess(dataPointId2, 2), new DataPointAccess(dataPointId3, 2)).collect(Collectors.toList()),
                    new ArrayList<>(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    1,
                    2,
                    Stream.of(new DataPointAccess(dataPointId1, 1), new DataPointAccess(dataPointId2, 2), new DataPointAccess(dataPointId3, 2)).collect(Collectors.toList()),

            }, new Object[] {
                    Stream.of(new DataPointAccess(dataPointId1, 2), new DataPointAccess(dataPointId2, 2), new DataPointAccess(dataPointId3, 2)).collect(Collectors.toList()),
                    Stream.of(new DataPointAccess(dataPointId4, 2)).collect(Collectors.toList()),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    1,
                    2,
                    Stream.of(new DataPointAccess(dataPointId1, 2), new DataPointAccess(dataPointId2, 2), new DataPointAccess(dataPointId3, 2), new DataPointAccess(dataPointId4, 2)).collect(Collectors.toList()),

            }, new Object[] {
                    Stream.of(new DataPointAccess(dataPointId1, 1)).collect(Collectors.toList()),
                    Stream.of(new DataPointAccess(dataPointId1, 2), new DataPointAccess(dataPointId2, 2), new DataPointAccess(dataPointId3, 2)).collect(Collectors.toList()),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    1,
                    2,
                    Stream.of(new DataPointAccess(dataPointId1, 2), new DataPointAccess(dataPointId2, 2), new DataPointAccess(dataPointId3, 2)).collect(Collectors.toList())
            }, new Object[] {
                    Stream.of(new DataPointAccess(dataPointId4, 1)).collect(Collectors.toList()),
                    Stream.of(new DataPointAccess(dataPointId1, 2), new DataPointAccess(dataPointId2, 2), new DataPointAccess(dataPointId3, 2)).collect(Collectors.toList()),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    1,
                    2,
                    Stream.of(new DataPointAccess(dataPointId1, 2), new DataPointAccess(dataPointId2, 2), new DataPointAccess(dataPointId3, 2), new DataPointAccess(dataPointId4, 1)).collect(Collectors.toList()),

            }, new Object[] {
                    Stream.of(new DataPointAccess(dataPointId1, 1), new DataPointAccess(dataPointId2, 2), new DataPointAccess(dataPointId3, 2)).collect(Collectors.toList()),
                    Stream.of(new DataPointAccess(dataPointId1, 2)).collect(Collectors.toList()),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    1,
                    2,
                    Stream.of(new DataPointAccess(dataPointId1, 2), new DataPointAccess(dataPointId2, 2), new DataPointAccess(dataPointId3, 2)).collect(Collectors.toList())
            }, new Object[] {
                    Stream.of(new DataPointAccess(dataPointId1, 1), new DataPointAccess(dataPointId2, 2), new DataPointAccess(dataPointId3, 2)).collect(Collectors.toList()),
                    Stream.of(new DataPointAccess(dataPointId1, 2), new DataPointAccess(dataPointId2, 2)).collect(Collectors.toList()),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    1,
                    2,
                    Stream.of(new DataPointAccess(dataPointId1, 2), new DataPointAccess(dataPointId2, 2), new DataPointAccess(dataPointId3, 2)).collect(Collectors.toList())
            }, new Object[] {
                    Stream.of(new DataPointAccess(dataPointId1, 1), new DataPointAccess(dataPointId2, 2)).collect(Collectors.toList()),
                    Stream.of(new DataPointAccess(dataPointId1, 2), new DataPointAccess(dataPointId2, 1)).collect(Collectors.toList()),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    1,
                    2,
                    Stream.of(new DataPointAccess(dataPointId1, 2), new DataPointAccess(dataPointId2, 2)).collect(Collectors.toList()),

            }, new Object[] {
                    Stream.of(new DataPointAccess(dataPointId1, 1), new DataPointAccess(dataPointId2, 2)).collect(Collectors.toList()),
                    Stream.of(new DataPointAccess(dataPointId1, 1), new DataPointAccess(dataPointId2, 2)).collect(Collectors.toList()),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    1,
                    2,
                    Stream.of(new DataPointAccess(dataPointId1, 1), new DataPointAccess(dataPointId2, 2)).collect(Collectors.toList())
            }, new Object[] {
                    new ArrayList<>(),
                    Stream.of(new DataPointAccess(dataPointId1, 1), new DataPointAccess(dataPointId2, 2), new DataPointAccess(dataPointId2, 2)).collect(Collectors.toList()),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    1,
                    2,
                    Stream.of(new DataPointAccess(dataPointId1, 1), new DataPointAccess(dataPointId2, 2)).collect(Collectors.toList())
            }
        };
    }

    private List<DataPointAccess> expected;

    public DataPointMigrationPermissionsCommandTest(List<DataPointAccess> fromProfile,
                                                    List<DataPointAccess> fromUser,
                                                    List<Integer> fromView1,
                                                    List<Integer> fromView2,
                                                    int permissionView1,
                                                    int permissionView2,
                                                    List<DataPointAccess> expected) throws DAOException {
        this.expected = expected;
        this.expected.sort(Comparator.comparingInt(DataPointAccess::getDataPointId));

        UsersProfileVO profile = new UsersProfileVO();
        profile.setId(123);
        profile.setXid("UP_TEST_1_");
        profile.setName("profileName");
        profile.setDataPointPermissions(fromProfile);

        int userId = 1234;
        user = TestUtils.newUser(userId);
        user.setUserProfile(profile);
        users = Arrays.asList(user);

        Map<Integer, List<DataPointAccess>> userPermissions = new HashMap<>();
        userPermissions.put(user.getId(), fromUser);

        Map<Integer, UsersProfileVO> userProfiles = new HashMap<>();
        userProfiles.put(user.getId(), profile);

        Map<Integer, UsersProfileVO> profiles = new HashMap<>();
        profiles.put(profile.getId(), profile);

        UsersProfileDAO usersProfileDAO = new UsersProfileDAOMemory(profiles, userProfiles);
        DataPointUserDAO dataPointUserDAO = new DataPointUserDAOMemory(usersProfileDAO);

        profilePermissionsService = new DataPointProfilePermissionsService(usersProfileDAO, dataPointUserDAO);
        userPermissionsService = new PermissionsServiceUserTestImpl<>(userPermissions);

        PermissionsService<WatchListAccess, User> watchListUserPermissionsService = new PermissionsServiceUserTestImpl<>(new HashMap<>());
        PermissionsService<Integer, User> dataSourceUserPermissionsService = new PermissionsServiceUserTestImpl<>(new HashMap<>());
        PermissionsService<ViewAccess, User> viewUserPermissionsService = new PermissionsServiceUserTestImpl<>(new HashMap<>());

        PermissionsService<WatchListAccess, UsersProfileVO> watchListPermissionsService = new PermissionsServiceProfileTestImpl<>(new HashMap<>());
        PermissionsService<Integer, UsersProfileVO> dataSourcePermissionsService = new PermissionsServiceProfileTestImpl<>(new HashMap<>());
        PermissionsService<ViewAccess, UsersProfileVO> viewPermissionsService = new PermissionsServiceProfileTestImpl<>(new HashMap<>());


        DataPointVO dataPoint1 = new DataPointVO(DataPointVO.LoggingTypes.ON_CHANGE);
        dataPoint1.setId(dataPointId1);

        DataPointVO dataPoint2 = new DataPointVO(DataPointVO.LoggingTypes.ON_CHANGE);
        dataPoint2.setId(dataPointId2);

        DataPointVO dataPoint3 = new DataPointVO(DataPointVO.LoggingTypes.ON_CHANGE);
        dataPoint3.setId(dataPointId3);

        DataPointVO dataPoint4 = new DataPointVO(DataPointVO.LoggingTypes.ON_CHANGE);
        dataPoint4.setId(dataPointId4);

        DataPointVO dataPoint5 = new DataPointVO(DataPointVO.LoggingTypes.ON_CHANGE);
        dataPoint5.setId(dataPointId5);

        List<DataPointVO> dataPoints = Arrays.asList(dataPoint1, dataPoint2, dataPoint3, dataPoint4, dataPoint5);

        views = new ArrayList<>();
        views.add(ViewTestUtils.newView(dataPoints, fromView1, new ShareUser(user.getId(), permissionView1)));
        views.add(ViewTestUtils.newView(dataPoints, fromView2, new ShareUser(user.getId(), permissionView2)));


        DAO dao = mock(DAO.class);
        when(dao.generateUniqueXid(UsersProfileVO.XID_PREFIX, "usersProfiles")).thenAnswer(a -> {
                String pre = (String)a.getArguments()[0];
                return pre + new Random().nextInt();
        });

        MangoDataPoint dataPointService = mock(DataPointService.class);
        when(dataPointService.getDataPoint(anyInt())).thenAnswer(a -> {
            int id = (int)a.getArguments()[0];
            return dataPoints.stream().filter(dataPoint -> dataPoint.getId() == id).findAny().orElse(null);
        });

        ViewService viewService = mock(ViewService.class);
        when(viewService.getViews()).thenReturn(views);

        MangoDataSource dataSourceService = mock(DataSourceService.class);
        WatchListService watchListService = mock(WatchListService.class);

        UsersProfileService usersProfileService = new UsersProfileService(usersProfileDAO, dao, null,
                watchListPermissionsService, profilePermissionsService,
                dataSourcePermissionsService, viewPermissionsService);

        migrationPermissionsService = new MigrationPermissionsService(userPermissionsService,
                dataSourceUserPermissionsService, watchListUserPermissionsService, viewUserPermissionsService);

        migrationDataService = new MigrationDataService(dataPointService,
                dataSourceService, viewService, watchListService, usersProfileService);

    }


    private PermissionsService<DataPointAccess, UsersProfileVO> profilePermissionsService;
    private PermissionsService<DataPointAccess, User> userPermissionsService;

    private User user;

    private MigrationPermissionsService migrationPermissionsService;
    private MigrationDataService migrationDataService;
    private List<User> users;
    private List<View> views;

    @Test
    public void when_execute_then_datapointaccesses() {
        //given:
        MigrationPermissions migrationCommand = MigrationPermissions
                .newMigration(migrationPermissionsService, migrationDataService, views);

        //when:
        migrationCommand.execute(users);

        //then:
        List<DataPointAccess> dataPointAccesses = userPermissionsService.getPermissions(user);

        UsersProfileVO usersProfileVO = new UsersProfileVO();
        usersProfileVO.setId(user.getUserProfile());

        List<DataPointAccess> dataPointAccessesFromProfile = profilePermissionsService.getPermissions(usersProfileVO);

        List<DataPointAccess> result = mergeDataPointAccessesList(dataPointAccesses, dataPointAccessesFromProfile);
        result.sort(Comparator.comparingInt(DataPointAccess::getDataPointId));

        assertEquals(expected, result);
    }
}