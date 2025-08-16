export default (sequelize, DataTypes) => {
  const Stock = sequelize.define(
    "Stock",
    {
      symbol: {
        type: DataTypes.STRING,
        allowNull: false,
        primaryKey: true,
      },
      name: {
        type: DataTypes.STRING,
        allowNull: true,
      },
      basdt: {
        type: DataTypes.DATEONLY,
        allowNull: false,
      },
      isincd: {
        type: DataTypes.STRING,
        allowNull: true,
      },
      mrktctg: {
        type: DataTypes.STRING,
        allowNull: true,
      },
      crno: {
        type: DataTypes.STRING,
        allowNull: true,
      },
      corpnm: {
        type: DataTypes.STRING,
        allowNull: true,
      },
    },
    {
      tableName: "stock_table",
      timestamps: false, // createdAt, updatedAt 자동 생성 방지
      underscored: true, // snake_case 컬럼 매핑
    }
  );

  return Stock;
};
