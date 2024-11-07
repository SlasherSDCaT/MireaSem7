// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract SecuritiesExchange {
    address public owner;

    struct Security {
        string name;
        uint256 price; // Цена одной единицы ценной бумаги
    }

    mapping(string => Security) public securities;
    mapping(address => mapping(string => uint256)) public balances;

    event SecurityPurchased(address indexed buyer, string security, uint256 amount);
    event SecurityExchanged(
        address indexed trader,
        string fromSecurity,
        string toSecurity,
        uint256 amountExchanged,
        uint256 amountReceived,
        uint256 remainderReturned
    );

    modifier onlyOwner() {
        require(msg.sender == owner, "Only the owner can perform this function");
        _;
    }

    constructor() {
        owner = msg.sender;
    }

    function addSecurity(string memory name, uint256 price) public onlyOwner {
        securities[name] = Security(name, price);
    }

    function updateSecurityPrice(string memory name, uint256 newPrice) public onlyOwner {
        require(securities[name].price != 0, "Security not found");
        securities[name].price = newPrice;
    }

    function purchaseSecurity(string memory name, uint256 amount) public payable {
        require(securities[name].price != 0, "Security not found");
        uint256 totalPrice = securities[name].price * amount;
        require(msg.value >= totalPrice, "Insufficient funds to purchase");

        balances[msg.sender][name] += amount;

        if (msg.value > totalPrice) {
            payable(msg.sender).transfer(msg.value - totalPrice);
        }

        emit SecurityPurchased(msg.sender, name, amount);
    }

    function exchangeSecurities(string memory fromSecurity, string memory toSecurity, uint256 amount) public {
        require(securities[fromSecurity].price != 0, "The first security does not exist");
        require(securities[toSecurity].price != 0, "The second security does not exist.");
        require(balances[msg.sender][fromSecurity] >= amount, "Not enough securities to exchange");

        uint256 fromPrice = securities[fromSecurity].price;
        uint256 toPrice = securities[toSecurity].price;

        uint256 totalValue = amount * fromPrice;
        uint256 amountToReceive = totalValue / toPrice;
        uint256 remainderValue = totalValue % toPrice;

        balances[msg.sender][fromSecurity] -= amount;
        balances[msg.sender][toSecurity] += amountToReceive;

        if (remainderValue > 0) {
            payable(msg.sender).transfer(remainderValue);
        }

        emit SecurityExchanged(
            msg.sender,
            fromSecurity,
            toSecurity,
            amount,
            amountToReceive,
            remainderValue
        );
    }

    function getBalance(address user, string memory security) public view returns (uint256) {
        return balances[user][security];
    }

    function withdrawFunds(uint256 amount) public onlyOwner {
        require(address(this).balance >= amount, "Insufficient funds under contract");
        payable(owner).transfer(amount);
    }
}
