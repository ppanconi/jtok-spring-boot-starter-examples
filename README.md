# spring-jtok-examples
spring-jtok-examples is a project to demonstrate spring-jtok 
application to a trivial ecommerce company scenario with 3
bounded-contexts/micorservices: depot, ecommerce, payments.

The repository contains:

- **depot**: a gradle spring-jtok project modelling stock depot bounded context
- **ecommerce-mongo**: a gradle spring-jtok project modelling ecommerce portal bounded context on mongodb
- **payments-mongo**: a gradle spring-jtok project modelling payments system bounded context on mongodb
- **integration-tests**: a maven project to test the integration between
the three microservices. It provides java integration tests and IntelliJ Idea 
http script samples. It also provides docker artifacts:
  - a *Docker file* to build
  microservices images 
  - a *docker-compose* definition to startup all the needed systems
  locally, that is:
    - *zookeeper*
    - *kafka* broker
    - *kafdrop* (a web kafka monitoring tool)
    - a single multi database *postgres* instance (on local we use a single 
    postgres to reduce resource consumption, in a production scenario each 
    microservice has its database, see k8 charts)
    - *depot* microservice (scalable to multiple instances)
    - *ecommerce* microservice (scalable)
    - *payments* microservice (scalable)
    - a *nginx* api gateway instance  
-  **k8** e directory with helms charts to deploy all the system on your Kubernetes cluster 
  
## Run all locally 
You can run all the system locally using [docker-compose](https://docs.docker.com/compose/install/)
(active experimental DOCKER_BUILDKIT to build microservices images)
```bash
cd integration-tests/src/main/docker
./start_compose.sh

[+] Running 11/11
 ⠿ Network docker_app-tier        Created                                                                                                              0.8s
 ⠿ Container docker-mongo-1       Started                                                                                                              2.6s
 ⠿ Container docker-postgres-1    Started                                                                                                              3.3s
 ⠿ Container docker-zookeeper-1   Started                                                                                                              2.3s
 ⠿ Container docker-kafka-1       Started                                                                                                              4.3s
 ⠿ Container docker-mongosetup-1  Started                                                                                                              4.0s
 ⠿ Container docker-depot-1       Started                                                                                                              7.6s
 ⠿ Container docker-kafdrop-1     Started                                                                                                              7.8s
 ⠿ Container docker-payments-1    Started                                                                                                              6.3s
 ⠿ Container docker-ecommerce-1   Started                                                                                                              7.8s
 ⠿ Container docker-gateway-1     Started                                                                                                              9.5s
```
You can scale microservice instances
```bash
docker-compose up -d --no-recreate --scale depot=3 --scale ecommerce=2

[+] Running 13/13
 ⠿ Container docker-zookeeper-1   Running                                                                                                              0.0s
 ⠿ Container docker-kafka-1       Running                                                                                                              0.0s
 ⠿ Container docker-kafdrop-1     Running                                                                                                              0.0s
 ⠿ Container docker-postgres-1    Running                                                                                                              0.0s
 ⠿ Container docker-mongo-1       Running                                                                                                              0.0s
 ⠿ Container docker-mongosetup-1  Started                                                                                                              3.1s
 ⠿ Container docker-depot-1       Running                                                                                                              0.0s
 ⠿ Container docker-depot-3       Started                                                                                                              2.9s
 ⠿ Container docker-payments-1    Running                                                                                                              0.0s
 ⠿ Container docker-ecommerce-1   Running                                                                                                              0.0s
 ⠿ Container docker-ecommerce-2   Started                                                                                                              4.5s
 ⠿ Container docker-depot-2       Started                                                                                                              3.2s
 ⠿ Container docker-gateway-1     Running                                                                                                              0.0s

docker-compose ps
NAME                  COMMAND                  SERVICE             STATUS              PORTS
docker-depot-1        "sh -c 'exec java -c…"   depot               running             8080/tcp
docker-depot-2        "sh -c 'exec java -c…"   depot               running             8080/tcp
docker-depot-3        "sh -c 'exec java -c…"   depot               running             8080/tcp
docker-ecommerce-1    "sh -c 'exec java -c…"   ecommerce           running             8080/tcp
docker-ecommerce-2    "sh -c 'exec java -c…"   ecommerce           running             8080/tcp
docker-gateway-1      "/docker-entrypoint.…"   gateway             running             0.0.0.0:4000->4000/tcp, :::4000->4000/tcp
docker-kafdrop-1      "/kafdrop.sh"            kafdrop             running             0.0.0.0:9999->9000/tcp, :::9999->9000/tcp
docker-kafka-1        "/opt/bitnami/script…"   kafka               running             0.0.0.0:29092->29092/tcp, :::29092->29092/tcp
docker-mongo-1        "docker-entrypoint.s…"   mongo               running             127.0.0.1:28018->27017/tcp
docker-mongosetup-1   "bash -c 'sleep 10 &…"   mongosetup          exited (0)
docker-payments-1     "sh -c 'exec java -c…"   payments            running             8080/tcp
docker-postgres-1     "docker-entrypoint.s…"   postgres            running             127.0.0.1:5432->5432/tcp
docker-zookeeper-1    "/opt/bitnami/script…"   zookeeper           running             0.0.0.0:2181->2181/tcp, :::2181->2181/tcp

```
You can test the microservice endpoints and asynchronous domain events propagation using
the Intellij Idea http scripts in `integration-tests/src/main/http`
or the following curl commands:

Create payment account
```bash
curl -X POST --location "http://localhost:4000/payments/api/accounts" \
    -H "Content-Type: application/json" \
    -d "{
          \"userId\": \"panks\",
          \"notes\": \"Panks account\"
        }"
```

deposit 250 € on the account
```bash
curl -X POST --location "http://localhost:4000/payments/api/accounts/panks/deposit" \
    -H "Content-Type: application/json" \
    -d "250.00"
```

create articles in depot
```bash
curl -X POST --location "http://localhost:4000/depot/api/articles" \
    -H "Content-Type: application/json" \
    -d "{
          \"name\": \"scarpe-eleganti\",
          \"description\": \"scarpe eleganti\"
        }"
```

```bash
curl -X POST --location "http://localhost:4000/depot/api/articles" \
    -H "Content-Type: application/json" \
    -d "{
          \"name\": \"calze-seta\",
          \"description\": \"calze di seta\"
        }"
```

get the synchronized articles in the ecommerce catalog the quantity is still 0 

```bash
curl --location "http://localhost:4000/ecommerce/api/catalogArticles"
```


```json
{
  "_embedded" : {
    "catalogArticles" : [ {
      "version" : null,
      "lastOperationTs" : null,
      "name" : "scarpe-eleganti",
      "description" : "scarpe eleganti",
      "quantity" : 0,
      "_links" : {
        "self" : {
          "href" : "http://ecommerce:8080/api/catalogArticles/63c160cd09d9801e9cc66f9f"
        },
        "catalogArticle" : {
          "href" : "http://ecommerce:8080/api/catalogArticles/63c160cd09d9801e9cc66f9f"
        }
      }
    }, {
      "version" : null,
      "lastOperationTs" : null,
      "name" : "calze-seta",
      "description" : "calze di seta",
      "quantity" : 0,
      "_links" : {
        "self" : {
          "href" : "http://ecommerce:8080/api/catalogArticles/63c160fcfd076e3466b141c8"
        },
        "catalogArticle" : {
          "href" : "http://ecommerce:8080/api/catalogArticles/63c160fcfd076e3466b141c8"
        }
      }
    } ]
  },
  "_links" : {
    "self" : {
      "href" : "http://ecommerce:8080/api/catalogArticles"
    },
    "profile" : {
      "href" : "http://ecommerce:8080/api/profile/catalogArticles"
    },
    "search" : {
      "href" : "http://ecommerce:8080/api/catalogArticles/search"
    }
  }
}
```
add stocks into the depot

```bash
curl -X POST --location "http://localhost:4000/depot/api/operations/place" \
    -H "Content-Type: application/json" \
    -d "[
          {
            \"article\": \"scarpe-eleganti\",
            \"quantity\": 50
          },
          {
            \"article\": \"calze-seta\",
            \"quantity\": 250
          }
        ]"
```

see the ecommerce catalog updated with the right quantity

```bash
curl --location "http://localhost:4000/ecommerce/api/catalogArticles"
```

```json
{
  "_embedded" : {
    "catalogArticles" : [ {
      "version" : null,
      "lastOperationTs" : null,
      "name" : "scarpe-eleganti",
      "description" : "scarpe eleganti",
      "quantity" : 50,
      "_links" : {
        "self" : {
          "href" : "http://ecommerce:8080/api/catalogArticles/63c160cd09d9801e9cc66f9f"
        },
        "catalogArticle" : {
          "href" : "http://ecommerce:8080/api/catalogArticles/63c160cd09d9801e9cc66f9f"
        }
      }
    }, {
      "version" : null,
      "lastOperationTs" : null,
      "name" : "calze-seta",
      "description" : "calze di seta",
      "quantity" : 250,
      "_links" : {
        "self" : {
          "href" : "http://ecommerce:8080/api/catalogArticles/63c160fcfd076e3466b141c8"
        },
        "catalogArticle" : {
          "href" : "http://ecommerce:8080/api/catalogArticles/63c160fcfd076e3466b141c8"
        }
      }
    } ]
  },
  "_links" : {
    "self" : {
      "href" : "http://ecommerce:8080/api/catalogArticles"
    },
    "profile" : {
      "href" : "http://ecommerce:8080/api/profile/catalogArticles"
    },
    "search" : {
      "href" : "http://ecommerce:8080/api/catalogArticles/search"
    }
  }
}
```

post an order to the ecommerce api

```bash
curl -X POST --location "http://localhost:4000/ecommerce/api/order" \
    -H "Content-Type: application/json" \
    -d "{
          \"customer\": \"panks\",
          \"granTotal\": 123.56,
          \"itemValues\": [
            {\"article\": \"scarpe-eleganti\", \"quantity\": 1},
            {\"article\": \"calze-seta\", \"quantity\": 2}
          ]
        }"
```
```json
{"id":"63c1616109d9801e9cc66fa0","globalId":"9fe44f3f-e8d6-4382-b764-0eb5ae73c022","customer":"panks","status":"CREATED","currency":"EUR","granTotal":123.56,"notes":null,"items":[{"catalogArticleId":"63c160cd09d9801e9cc66f9f","catalogArticleName":"scarpe-eleganti","quantity":1},{"catalogArticleId":"63c160fcfd076e3466b141c8","catalogArticleName":"calze-seta","quantity":2}]}
```
Get the orderId from the previous response.
orderId=63c1616109d9801e9cc66fa0

check the order status (using the returned orderId)

```bash
curl --location http://localhost:4000/ecommerce/api/orders/{orderId}
```
```json
{
  "globalId" : "9fe44f3f-e8d6-4382-b764-0eb5ae73c022",
  "customer" : "panks",
  "status" : "APPROVED",
  "currency" : "EUR",
  "granTotal" : 123.56,
  "notes" : null,
  "items" : [ {
    "catalogArticleId" : "63c160cd09d9801e9cc66f9f",
    "catalogArticleName" : "scarpe-eleganti",
    "quantity" : 1
  }, {
    "catalogArticleId" : "63c160fcfd076e3466b141c8",
    "catalogArticleName" : "calze-seta",
    "quantity" : 2
  } ],
  "_links" : {
    "self" : {
      "href" : "http://ecommerce:8080/api/orders/63c1616109d9801e9cc66fa0"
    },
    "order" : {
      "href" : "http://ecommerce:8080/api/orders/63c1616109d9801e9cc66fa0"
    }
  }
}
```

the order status is APPROVED because articles goods are in stock. 
You can check the stock quantity updated on the depod side

```bash
curl --location http://localhost:4000/depot/api/articles
```

```json
{
  "_embedded" : {
    "articles" : [ {
      "lastOperationTs" : 1647601174494,
      "name" : "scarpe-eleganti",
      "description" : "scarpe eleganti",
      "handlingList" : [ {
        "quantity" : 50,
        "timestamp" : 1647601026608,
        "_links" : {
          "operation" : {
            "href" : "http://depot:8080/api/operations/4"
          },
          "article" : {
            "href" : "http://depot:8080/api/articles/1"
          }
        }
      }, {
        "quantity" : -1,
        "timestamp" : 1647601174492,
        "_links" : {
          "operation" : {
            "href" : "http://depot:8080/api/operations/7"
          },
          "article" : {
            "href" : "http://depot:8080/api/articles/1"
          }
        }
      } ],
      "stockQuantity" : 49,
      "_links" : {
        "self" : {
          "href" : "http://depot:8080/api/articles/1"
        },
        "article" : {
          "href" : "http://depot:8080/api/articles/1"
        }
      }
    }, {
      "lastOperationTs" : 1647601174494,
      "name" : "calze-seta",
      "description" : "calze di seta",
      "handlingList" : [ {
        "quantity" : 250,
        "timestamp" : 1647601026609,
        "_links" : {
          "operation" : {
            "href" : "http://depot:8080/api/operations/4"
          },
          "article" : {
            "href" : "http://depot:8080/api/articles/3"
          }
        }
      }, {
        "quantity" : -2,
        "timestamp" : 1647601174493,
        "_links" : {
          "operation" : {
            "href" : "http://depot:8080/api/operations/7"
          },
          "article" : {
            "href" : "http://depot:8080/api/articles/3"
          }
        }
      } ],
      "stockQuantity" : 248,
      "_links" : {
        "self" : {
          "href" : "http://depot:8080/api/articles/3"
        },
        "article" : {
          "href" : "http://depot:8080/api/articles/3"
        }
      }
    } ]
  },
  "_links" : {
    "self" : {
      "href" : "http://depot:8080/api/articles"
    },
    "profile" : {
      "href" : "http://depot:8080/api/profile/articles"
    },
    "search" : {
      "href" : "http://depot:8080/api/articles/search"
    }
  }
}
```

proceed to pay the order

```bash
curl -X PATCH --location "http://localhost:4000/ecommerce/api/order/{orderId}/checkout"
```

check the order status to PAYED

```bash
curl --location http://localhost:4000/ecommerce/api/orders/{orderId}
```

```json
{
  "globalId" : "9fe44f3f-e8d6-4382-b764-0eb5ae73c022",
  "customer" : "panks",
  "status" : "PAYED",
  "currency" : "EUR",
  "granTotal" : 123.56,
  "notes" : null,
  "items" : [ {
    "catalogArticleId" : "63c160cd09d9801e9cc66f9f",
    "catalogArticleName" : "scarpe-eleganti",
    "quantity" : 1
  }, {
    "catalogArticleId" : "63c160fcfd076e3466b141c8",
    "catalogArticleName" : "calze-seta",
    "quantity" : 2
  } ],
  "_links" : {
    "self" : {
      "href" : "http://ecommerce:8080/api/orders/63c1616109d9801e9cc66fa0"
    },
    "order" : {
      "href" : "http://ecommerce:8080/api/orders/63c1616109d9801e9cc66fa0"
    }
  }
}
```

and check the account balance on the payments microservice

```bash
curl --location http://localhost:4000/payments/api/accounts/panks/balance
```

```json
{
    "id": "63c160b88160e45f96462461",
    "version": null,
    "userId": "panks",
    "lastOperationTs": 1673617979043,
    "notes": "Panks account",
    "operations":
    [
        {
            "version": null,
            "amount": 250.00,
            "timestamp": 1673617601240,
            "refId": "ab2c8a2c-226e-471a-a913-d377ae672159",
            "refKey": "api"
        },
        {
            "version": null,
            "amount": -123.5600000000000022737367544323205947875976562500,
            "timestamp": 1673617979043,
            "refId": "a2e65baf-b3e4-40f5-ab12-26b3fa2c7fdc",
            "refKey": "9fe44f3f-e8d6-4382-b764-0eb5ae73c022"
        }
    ],
    "balance": 126.43
}
```
Nice job! You have completed some eventually consistent distributed sagas among the deployed microservices     

Stop and remove containers cleaning anonymous volumes

```bash
docker-compose down -v
```

To better understand the underlining event workflow we report 
some sagas graphical representations:

Depot articles to ecommerce catalog saga:

![article-catalog-sagas](doc/article-catalog-sagas.png "article-catalog-sagas")

Order and depot handling sagas 

![ordes-sagas](doc/ordes-sagas.png "ordes-sagas")

Order rejected for not available stock on depot 

![orders-saga-order-reject](doc/orders-saga-order-reject.png "orders-saga-order-reject")

Order expired for user latency expired or session abandoned

![orders-saga-order-expired](doc/orders-saga-order-expired.png "orders-saga-order-expired")
