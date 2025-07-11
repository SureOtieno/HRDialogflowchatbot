package com.example.dialogflow.utils.constants;


import org.springframework.stereotype.Component;

@Component
public class Constants {

    public final static String FAILED = "Failed: ";
    public static final String JWT_SECRET = "y8sK9vJr#4pL@qW1dXz!mT3bV&nC7gF2";
    public static final long JWT_TOKEN_VALIDITY = 30 * 24 * 60 * 60;
    public static final long JWT_REFRESH_TOKEN_VALIDITY = 90 * 24 * 60 * 60;
    public static final String DEFAULT_DB_NAME = "employees";
    public static final String RECORD_NOT_FOUND = "Record not found.";
    public static final String EMAIL_TAKEN = "Email taken.";
    public static final String PHONE_TAKEN = "Phone number already exists.";
    public static final String EMPLOYEE_CODE_TAKEN = "Employee code taken.";

    public static final String EMPLOYEE_EMAIL_TAKEN = "Employee Email taken.";
    public static final String PAGESIZE = "pageSize";

    public static final String PAGE = "page";
    public static final String INVALID_MISSING_TOKEN = "Invalid or missing token.";

    public static final String USERNAME = "userName";
    public static final String ACC_NAME = "accName";
    public static final String KRA_PIN = "kraPin";
    public static final String PHYSICAL_ADDRESS = "physicalAddress";
    public static final String MARITAL_STATUS = "maritalStatus";
    public static final String BANK = "bank";
    public static final String BRANCH = "branch";
    public static final String DESIGNATION = "designation";
    public static final String BRANCH_LOCATION = "branchLocation";
    public static final String BANK_LOCATION = "bankLocation";
    public static final String BANK_CODE = "bankCode";
    public static final String BRANCH_NAME = "branchName";
    public static final String BANK_BRANCH = "branches";
    public static final String NAMES = "fullNames";

    public static final String EMPLOYEE_ID = "employeeId";

    public static final String ID_NO = "idNo";

    public static final String PHONE_NUMBER = "phone";
    public static final String PHONE = "phoneNumber";


    public static final String EMAIL = "email";


    public static final String STARTDATE = "startDate";


    public static final String ENDDATE = "endDate";

    public static final String CREATED_ON = "createdOn";

    public static final String TOTALPAGES ="totalPages" ;
    public static final String TOTALCOUNT = "totalCount" ;

    public static final String NO_RECORD_FOUND = "No Records Found";


    public static final String EMP_TERMINATED = "empTerminated";

    public static final String ON_BOARDING_EMAIL = "ON_BOARDING_EMAIL";

    public final static String FRIENDLY_UNDEFINED_ERROR = "Failed: Something went wrong please contact Support or try again.";

    public static final String PREVIOUS_PERIOD = "previousPeriod";
    public static final String FREQUENCY = "frequency";
    public static final String NEXT_PERIOD = "nextPeriod";
    public static final String CURRENT_PERIOD = "currentPeriod";
    public static final String DESCRIPTION = "description";
    public static final String RECORD_EXISTS = "Record already exists.";

    public static final String NOT_FOUND = "Record not found";

    public static final String EMPLOYEE_RATE = "employeeRate";
    public static final String EMPLOYER_RATE = "employerRate";
    public static final String MAX_TIER_1 = "maxTier1";
    public static final String MAX_TIER_2 = "maxTier2";
    public static final String INVALID_ENTRY = "Invalid entry: ";

    public static final String MIN_AMOUNT = "minAmount";
    public static final String MAX_AMOUNT = "maxAmount";

    public static final String ITEM = "item";
    public static final String SUPERVISOR = "supervisor";
    public static final String DEPARTMENT = "department";

    public static final String SUCCESS = "Operation was successful.";

    public static final String CODE = "code";
    public static final String TYPE = "type";
    public static final String FIXED = "fixed";
    public static final String CATEGORY = "category";
    public static final String PRO_TYPE = "proType";
}

