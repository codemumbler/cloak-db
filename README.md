# cloak-db
Manages JNDI and an in memory db so you can concentrate on writing quick, clean DataSource unit tests.

Include the dependency using Maven:
```
  	<dependency>
	  <groupId>io.github.codemumbler</groupId>
	  <artifactId>cloak-db</artifactId>
	  <scope>test</scope>
	</dependency>
```

Then choose one of two ways to manage the mock database.

1)  Extend your test classes which touch the database with CloakAbstractTestCase and implement jdbcName to return your "jdbc/dbName".

```
  public class MyDBTestClass extends CloakAbstractTestCase {
    ....
    @Override
    protected String jdbcName() {
      return "jdbc/myDBName";
    }
    
    @Test
    public void testDBAction() {
      productionClass.doSomethingWithDatabase();
    }
    ...
  }
```

2) Use the CloakDatabase class to have more control when the database resets.

```
  public class MyDBTestClass {
  
    private static CloakDatabase database;
    ...
    
    @BeforeClass
	  public static void setUpClass() {
		  database = new CloakDatabase("jdbc/myDBName);
	  }

	  @After
	  public void tearDown() {
		  database.reset();
	  }
	  
	  ...
    
    @Test
    public void testDBAction() {
      productionClass.doSomethingWithDatabase();
    }
    
    ...
  }
```
