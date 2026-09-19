*** Settings ***
Library     RequestsLibrary
Library     JSONLibrary
Suite Setup     Create Session    api    http://localhost:8080    verify=false

*** Variables ***
${BASE_URL}     http://localhost:8080
${PRODUCTS_URL}     /api/products

*** Test Cases ***

Create Product
    [Documentation]    POST /api/products returns 201 with created product
    ${body}=    Create Dictionary
    ...    name=Test Widget
    ...    description=A test product
    ...    price=9.99
    ...    quantity=100
    ${response}=    POST On Session    api    ${PRODUCTS_URL}    json=${body}
    Should Be Equal As Integers    ${response.status_code}    201
    ${json}=    Set Variable    ${response.json()}
    Should Be Equal As Strings    ${json}[name]    Test Widget
    Should Be Equal As Strings    ${json}[price]    9.99
    Set Suite Variable    ${PRODUCT_ID}    ${json}[id]

Get All Products
    [Documentation]    GET /api/products returns 200 with a list
    ${response}=    GET On Session    api    ${PRODUCTS_URL}
    Should Be Equal As Integers    ${response.status_code}    200
    ${json}=    Set Variable    ${response.json()}
    Should Not Be Empty    ${json}

Get Product By ID
    [Documentation]    GET /api/products/{id} returns 200 with the product
    ${response}=    GET On Session    api    ${PRODUCTS_URL}/${PRODUCT_ID}
    Should Be Equal As Integers    ${response.status_code}    200
    ${json}=    Set Variable    ${response.json()}
    Should Be Equal As Integers    ${json}[id]    ${PRODUCT_ID}

Update Product
    [Documentation]    PUT /api/products/{id} returns 200 with updated data
    ${body}=    Create Dictionary
    ...    name=Updated Widget
    ...    description=Updated description
    ...    price=19.99
    ...    quantity=50
    ${response}=    PUT On Session    api    ${PRODUCTS_URL}/${PRODUCT_ID}    json=${body}
    Should Be Equal As Integers    ${response.status_code}    200
    ${json}=    Set Variable    ${response.json()}
    Should Be Equal As Strings    ${json}[name]    Updated Widget

Get Product Not Found
    [Documentation]    GET /api/products/99999 returns 404
    ${response}=    GET On Session    api    ${PRODUCTS_URL}/99999
    ...    expected_status=404
    Should Be Equal As Integers    ${response.status_code}    404

Create Product Validation Failure
    [Documentation]    POST with missing required fields returns 400
    ${body}=    Create Dictionary    description=No name or price
    ${response}=    POST On Session    api    ${PRODUCTS_URL}    json=${body}
    ...    expected_status=400
    Should Be Equal As Integers    ${response.status_code}    400

Delete Product
    [Documentation]    DELETE /api/products/{id} returns 204
    ${response}=    DELETE On Session    api    ${PRODUCTS_URL}/${PRODUCT_ID}
    Should Be Equal As Integers    ${response.status_code}    204

Verify Product Deleted
    [Documentation]    GET after delete returns 404
    ${response}=    GET On Session    api    ${PRODUCTS_URL}/${PRODUCT_ID}
    ...    expected_status=404
    Should Be Equal As Integers    ${response.status_code}    404
