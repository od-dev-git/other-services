package org.egov.report.repository.rowmapper;

import static java.util.Objects.isNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.report.model.enums.AddressType;
import org.egov.report.model.enums.UserType;
import org.egov.report.user.Address;
import org.egov.report.user.Role;
import org.egov.report.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import static org.egov.report.model.enums.AddressType.PERMANENT;
import static org.egov.report.model.enums.AddressType.CORRESPONDENCE;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserResultSetExtractor implements ResultSetExtractor<List<User>> {

    private ObjectMapper objectMapper;

    @Autowired
    UserResultSetExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<User> extractData(ResultSet rs) throws SQLException, DataAccessException {

        Map<Long, User> usersMap = new LinkedHashMap<>();

        while (rs.next()) {

            Long userId = rs.getLong("id");
            User user;

            if (!usersMap.containsKey(userId)) {

                user = User.builder().id(rs.getLong("id")).tenantId(rs.getString("tenantid")).title(rs.getString("title"))
                        .salutation(rs.getString("salutation"))
                        .dob(rs.getDate("dob")).username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .passwordExpiryDate(rs.getTimestamp("pwdexpirydate"))
                        .mobileNumber(rs.getString("mobilenumber")).altContactNumber(rs.getString("altcontactnumber"))
                        .emailId(rs.getString("emailid")).active(rs.getBoolean("active")).name(rs.getString("name")).
                                lastModifiedBy(rs.getLong("lastmodifiedby")).lastModifiedDate(rs.getTimestamp("lastmodifieddate"))
                        .pan(rs.getString("pan")).aadhaarNumber(rs.getString("aadhaarnumber")).createdBy(rs.getLong("createdby"))
                        .createdDate(rs.getTimestamp("createddate")).signature(rs.getString("signature"))
                        .accountLocked(rs.getBoolean("accountlocked")).photo(rs.getString("photo"))
                        .identificationMark(rs.getString("identificationmark")).uuid(rs.getString("uuid"))
                        .accountLockedDate(rs.getLong("accountlockeddate"))
                        .build();

                for (UserType type : UserType.values()) {
                    if (type.toString().equals(rs.getString("type"))) {
                        user.setType(type);
                    }
                }


//                for (BloodGroup bloodGroup : BloodGroup.values()) {
//                    if (bloodGroup.toString().equals(rs.getString("bloodgroup"))) {
//                        user.setBloodGroup(bloodGroup);
//                    }
//                }

//                if (rs.getInt("gender") == 1) {
//                    user.setGender(Gender.FEMALE);
//                } else if (rs.getInt("gender") == 2) {
//                    user.setGender(Gender.MALE);
//                } else if (rs.getInt("gender") == 3) {
//                    user.setGender(Gender.OTHERS);
//                } else if (rs.getInt("gender") == 4) {
//                    user.setGender(Gender.TRANSGENDER);    
//                }
//                for (GuardianRelation guardianRelation : GuardianRelation.values()) {
//                    if (guardianRelation.toString().equals(rs.getString("guardianrelation"))) {
//                        user.setGuardianRelation(guardianRelation);
//                    }
//                }

                usersMap.put(userId, user);

            } else {
                user = usersMap.get(userId);
            }

            Role role = populateRole(rs);
            Address address = populateAddress(rs, user);

            if (!isNull(role))
                user.addRolesItem(role);

            if (!isNull(address))
                user.addAddressItem(address);

        }

        return new ArrayList<>(usersMap.values());
    }

    private Role populateRole(ResultSet rs) throws SQLException {
        String code = rs.getString("role_code");
        if (code == null) {
            return null;
        }
        return Role.builder()
                .tenantId(rs.getString("role_tenantid"))
                .code(code)
                .build();
    }

    private Address populateAddress(ResultSet rs, User user) throws SQLException {
        long id = rs.getLong("addr_id");
        if (id == 0L) {
            return null;
        }

        Address address = Address.builder()
                .id(rs.getLong("addr_id"))
                .addressType(rs.getString("addr_type"))
                .type(AddressType.fromValue(rs.getString("addr_type")))
                .address(rs.getString("addr_address"))
                .city(rs.getString("addr_city"))
                .pinCode(rs.getString("addr_pincode"))
                .userId(rs.getLong("addr_userid"))
                .tenantId(rs.getString("addr_tenantid"))
                .build();

        if (address.getType().equals(PERMANENT) && isNull(user.getPermanentAddress()))
            user.setPermanentAddress(address);
        if (address.getType().equals(CORRESPONDENCE) && isNull(user.getCorrespondenceAddress()))
            user.setCorrespondenceAddress(address);

        return address;

    }
}