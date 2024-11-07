const { expect } = require("chai");
const { ethers } = require("hardhat");

describe("SecuritiesExchange", function () {
    let SecuritiesExchange;
    let securitiesExchange;
    let owner, addr1, addr2;

    beforeEach(async function () {
        SecuritiesExchange = await ethers.getContractFactory("SecuritiesExchange");
        [owner, addr1, addr2] = await ethers.getSigners();
        securitiesExchange = await SecuritiesExchange.deploy();
        await securitiesExchange.waitForDeployment();

        await securitiesExchange.addSecurity("Sec_1", 3);
        await securitiesExchange.addSecurity("Sec_2", 7);
    });

    it("should allow purchasing securities", async function () {
        const purchaseAmount = 3;
        const totalCost = 9;

        await securitiesExchange.connect(addr1).purchaseSecurity("Sec_1", purchaseAmount, { value: totalCost });

        const balance = await securitiesExchange.getBalance(addr1.address, "Sec_1");
        expect(balance).to.equal(purchaseAmount);
    });    


    it("should fail if there is insufficient balance for purchase", async function () {
        const purchaseAmount = 3;
        const insufficientAmount = 8;

        await expect(
            securitiesExchange.connect(addr1).purchaseSecurity("Sec_1", purchaseAmount, { value: insufficientAmount })
        ).to.be.revertedWith("Insufficient funds to purchase");
    });

    it("should fail if trying to exchange more securities than owned", async function () {
        await securitiesExchange.connect(addr1).purchaseSecurity("Sec_1", 1, { value: 3 });

        await expect(
            securitiesExchange.connect(addr1).exchangeSecurities("Sec_1", "Sec_2", 2)
        ).to.be.revertedWith("Not enough securities to exchange");
    });
    

    it("should not allow non-owner to withdraw funds", async function () {
        await expect(
            securitiesExchange.connect(addr1).withdrawFunds(1)
        ).to.be.revertedWith("Only the owner can perform this function");
    });
    
});
